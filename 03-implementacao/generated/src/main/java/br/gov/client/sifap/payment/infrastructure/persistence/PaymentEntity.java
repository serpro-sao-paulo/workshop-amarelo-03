package br.gov.client.sifap.payment.infrastructure.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA mapeada a partir do DDM Adabas PAGAMENTO (FNR 152).
 *
 * <p>Fonte legada: {@code 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm}</p>
 * <p>Bounded context: {@code payment} — Processamento de Pagamentos</p>
 * <p>Superdescriptors Adabas mapeados como índices compostos:
 * <ul>
 *   <li>S1 = cpf + referenceMonth  → idx_payment_cpf_month</li>
 *   <li>S2 = programCode + referenceMonth + status → idx_payment_prog_month_status</li>
 *   <li>S3 = cycleNumber + status  → idx_payment_cycle_status</li>
 * </ul>
 * </p>
 *
 * <p>Campos MU: nenhum neste DDM.</p>
 * <p>Campos PE: GRP-DESCONTO (CA-CG, max 8 ocorrências) → {@link DeductionEntity} via @OneToMany.</p>
 *
 * <p>Tamanho médio de registro original: ~720 bytes | ~180M registros (abr/2018)</p>
 *
 * @see DeductionEntity
 */
@Entity
@Table(
    name = "payment",
    indexes = {
        @Index(name = "idx_payment_cpf_month",          columnList = "cpf, reference_month"),
        @Index(name = "idx_payment_prog_month_status",  columnList = "program_code, reference_month, status"),
        @Index(name = "idx_payment_cycle_status",       columnList = "cycle_number, status"),
        @Index(name = "idx_payment_generation_date",    columnList = "generation_date"),
        @Index(name = "idx_payment_cpf",                columnList = "cpf")
    }
)
public class PaymentEntity {

    // ── IDENTIFICAÇÃO (DDM: AA–AF) ────────────────────────────────────────────

    /**
     * Sequencial único do pagamento.
     * DDM: AA / NUM-PAGAMENTO (N15) — SEQUENCIAL UNICO (DE)
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * CPF do beneficiário (sem formatação — 11 dígitos).
     * DDM: AB / NUM-CPF (A11) — CPF BENEFICIARIO (DE)
     * SECURITY: nunca expor em logs sem mascaramento (REQ: mascaramento de CPF).
     */
    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    /**
     * Matrícula / inscrição do beneficiário no programa.
     * DDM: AC / NUM-INSCRICAO (N11)
     */
    @Column(name = "enrollment_number")
    private Long enrollmentNumber;

    /**
     * Código do programa social.
     * DDM: AD / COD-PROGRAMA (A4) — PROG SOCIAL (DE)
     */
    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    /**
     * Competência de referência no formato YYYYMM.
     * DDM: AE / ANO-MES-REF (N6) — AAAAMM (DE)
     * Armazenado como Integer (ex: 202604) por compatibilidade com buscas e ordenação.
     */
    @Column(name = "reference_month", nullable = false)
    private Integer referenceMonth;

    /**
     * Número do ciclo de processamento batch.
     * DDM: AF / NUM-CICLO (N6)
     */
    @Column(name = "cycle_number")
    private Integer cycleNumber;

    // ── VALORES (DDM: BA–BC) ──────────────────────────────────────────────────

    /**
     * Valor bruto calculado.
     * DDM: BA / VLR-BRUTO (N9.2)
     */
    @Column(name = "gross_amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal grossAmount;

    /**
     * Valor líquido = bruto − descontos.
     * DDM: BB / VLR-LIQUIDO (N9.2)
     */
    @Column(name = "net_amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal netAmount;

    /**
     * Soma total de descontos aplicados.
     * DDM: BC / VLR-DESCONTO-TOTAL (N7.2)
     */
    @Column(name = "total_deduction", nullable = false, precision = 9, scale = 2)
    private BigDecimal totalDeduction;

    // ── DESCONTOS DETALHADOS (DDM: CA–CG — PE max 8) ─────────────────────────

    /**
     * Grupo periódico GRP-DESCONTO (PE, max 8 ocorrências).
     * DDM: CA / GRP-DESCONTO — mapeado como tabela filha {@link DeductionEntity}.
     * Adabas PE → @OneToMany (relação 1:N com chave estrangeira payment_id).
     */
    @OneToMany(
        mappedBy = "payment",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<DeductionEntity> deductions = new ArrayList<>();

    // ── STATUS E PROCESSAMENTO (DDM: DA–DG) ──────────────────────────────────

    /**
     * Situação do pagamento.
     * DDM: DA / SIT-PAGAMENTO (A1)
     * Valores: P=PENDENTE G=GERADO E=EMITIDO C=CONFIRMADO D=DEVOLVIDO X=CANCELADO R=REPROCESSADO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    /**
     * Data de geração pelo batch.
     * DDM: DB / DT-GERACAO (N8 AAAAMMDD) — (DE)
     */
    @Column(name = "generation_date")
    private LocalDate generationDate;

    /**
     * Hora de geração (HHMMSS).
     * DDM: DC / HR-GERACAO (N6)
     * Combinado com generation_date para formar LocalDateTime quando necessário.
     */
    @Column(name = "generation_time")
    private Integer generationTime;

    /**
     * Data de emissão ao banco.
     * DDM: DD / DT-EMISSAO (N8)
     */
    @Column(name = "emission_date")
    private LocalDate emissionDate;

    /**
     * Data de confirmação de retorno bancário.
     * DDM: DE / DT-CONFIRMACAO (N8)
     */
    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    /**
     * Data de cancelamento (se aplicável).
     * DDM: DF / DT-CANCELAMENTO (N8)
     */
    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    /**
     * Código do motivo de cancelamento (tabela interna).
     * DDM: DG / MOT-CANCELAMENTO (A3)
     * FIXME: confirm semantics — mapeamento de códigos não documentado no DDM
     */
    @Column(name = "cancellation_reason_code", length = 3)
    private String cancellationReasonCode;

    // ── DADOS BANCÁRIOS (DDM: EA–EE) ─────────────────────────────────────────

    /**
     * Código do banco (FEBRABAN).
     * DDM: EA / COD-BANCO (A3)
     */
    @Column(name = "bank_code", length = 3)
    private String bankCode;

    /**
     * Número da agência.
     * DDM: EB / COD-AGENCIA (A6)
     */
    @Column(name = "agency_code", length = 6)
    private String agencyCode;

    /**
     * Número da conta.
     * DDM: EC / NUM-CONTA (A13)
     */
    @Column(name = "account_number", length = 13)
    private String accountNumber;

    /**
     * Tipo de conta: C=CORRENTE P=POUPANCA.
     * DDM: ED / TIPO-CONTA (A1)
     */
    @Column(name = "account_type", length = 1)
    private String accountType;

    /**
     * Operação Caixa Econômica (se aplicável).
     * DDM: EE / COD-OPERACAO (A3)
     */
    @Column(name = "operation_code", length = 3)
    private String operationCode;

    // ── INTEGRAÇÃO SIAFI (DDM: FA–FE — adicionado 2002) ─────────────────────

    /**
     * Número da Ordem Bancária SIAFI.
     * DDM: FA / NUM-OB-SIAFI (A12)
     */
    @Column(name = "siafi_order_number", length = 12)
    private String siafiOrderNumber;

    /**
     * Número da Nota de Empenho SIAFI.
     * DDM: FB / NUM-NE-SIAFI (A12)
     */
    @Column(name = "siafi_commitment_number", length = 12)
    private String siafiCommitmentNumber;

    /**
     * Código da Unidade Gestora emitente.
     * DDM: FC / COD-UG-EMITENTE (A6)
     */
    @Column(name = "management_unit_code", length = 6)
    private String managementUnitCode;

    /**
     * Código de Gestão SIAFI.
     * DDM: FD / COD-GESTAO (A5)
     */
    @Column(name = "management_code", length = 5)
    private String managementCode;

    /**
     * Situação da integração SIAFI: I=INTEGRADO P=PENDENTE E=ERRO.
     * DDM: FE / SIT-INTEG-SIAFI (A1)
     */
    @Column(name = "siafi_integration_status", length = 1)
    private String siafiIntegrationStatus;

    // ── CONCILIAÇÃO BANCÁRIA (DDM: GA–GE) ────────────────────────────────────

    /**
     * Data de conciliação bancária.
     * DDM: GA / DT-CONCILIACAO (N8)
     */
    @Column(name = "reconciliation_date")
    private LocalDate reconciliationDate;

    /**
     * Situação da conciliação: C=CONCIL D=DIVERG P=PEND N=NA.
     * DDM: GB / SIT-CONCILIACAO (A1)
     */
    @Column(name = "reconciliation_status", length = 1)
    private String reconciliationStatus;

    /**
     * Valor confirmado pelo banco.
     * DDM: GC / VLR-CONCILIADO (N9.2)
     */
    @Column(name = "reconciled_amount", precision = 11, scale = 2)
    private BigDecimal reconciledAmount;

    /**
     * Código de retorno CNAB 240.
     * DDM: GD / COD-RETORNO-BANCO (A2)
     */
    @Column(name = "bank_return_code", length = 2)
    private String bankReturnCode;

    /**
     * Descrição do retorno bancário.
     * DDM: GE / DES-RETORNO-BANCO (A40)
     */
    @Column(name = "bank_return_description", length = 40)
    private String bankReturnDescription;

    // ── HASH DE ARQUIVO (DDM: HA–HB — adicionado 2015) ───────────────────────

    /**
     * SHA-256 do arquivo de remessa.
     * DDM: HA / HASH-ARQ-REMESSA (A64)
     */
    @Column(name = "remittance_file_hash", length = 64)
    private String remittanceFileHash;

    /**
     * SHA-256 do arquivo de retorno.
     * DDM: HB / HASH-ARQ-RETORNO (A64)
     */
    @Column(name = "return_file_hash", length = 64)
    private String returnFileHash;

    // ── CONTROLE DE AUDITORIA (DDM: IA–IF) ───────────────────────────────────

    /**
     * Data de inclusão do registro.
     * DDM: IA / DT-INCLUSAO (N8)
     */
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDate createdDate;

    /**
     * Hora de inclusão (HHMMSS).
     * DDM: IB / HR-INCLUSAO (N6)
     */
    @Column(name = "created_time", updatable = false)
    private Integer createdTime;

    /**
     * Login do usuário que incluiu (geralmente 'BATCH').
     * DDM: IC / USR-INCLUSAO (A8)
     */
    @Column(name = "created_by", length = 8, updatable = false)
    private String createdBy;

    /**
     * Data da última alteração.
     * DDM: ID / DT-ULT-ALTERACAO (N8)
     */
    @Column(name = "last_updated_date")
    private LocalDate lastUpdatedDate;

    /**
     * Hora da última alteração (HHMMSS).
     * DDM: IE / HR-ULT-ALTERACAO (N6)
     */
    @Column(name = "last_updated_time")
    private Integer lastUpdatedTime;

    /**
     * Login do usuário que fez a última alteração.
     * DDM: IF / USR-ULT-ALTERACAO (A8)
     */
    @Column(name = "last_updated_by", length = 8)
    private String lastUpdatedBy;

    // ── CONSTRUTORES ──────────────────────────────────────────────────────────

    protected PaymentEntity() {
        // JPA
    }

    public PaymentEntity(Long id, String cpf, String programCode, Integer referenceMonth,
                         BigDecimal grossAmount, BigDecimal netAmount, BigDecimal totalDeduction,
                         PaymentStatus status, LocalDate createdDate, String createdBy) {
        this.id = id;
        this.cpf = cpf;
        this.programCode = programCode;
        this.referenceMonth = referenceMonth;
        this.grossAmount = grossAmount;
        this.netAmount = netAmount;
        this.totalDeduction = totalDeduction;
        this.status = status;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
    }

    // ── GETTERS (sem setters públicos — use métodos de domínio) ──────────────

    public Long getId()                        { return id; }
    public String getCpf()                     { return cpf; }
    public Long getEnrollmentNumber()          { return enrollmentNumber; }
    public String getProgramCode()             { return programCode; }
    public Integer getReferenceMonth()         { return referenceMonth; }
    public Integer getCycleNumber()            { return cycleNumber; }
    public BigDecimal getGrossAmount()         { return grossAmount; }
    public BigDecimal getNetAmount()           { return netAmount; }
    public BigDecimal getTotalDeduction()      { return totalDeduction; }
    public List<DeductionEntity> getDeductions() { return List.copyOf(deductions); }
    public PaymentStatus getStatus()           { return status; }
    public LocalDate getGenerationDate()       { return generationDate; }
    public LocalDate getEmissionDate()         { return emissionDate; }
    public LocalDate getConfirmationDate()     { return confirmationDate; }
    public LocalDate getCancellationDate()     { return cancellationDate; }
    public String getCancellationReasonCode()  { return cancellationReasonCode; }
    public String getBankCode()                { return bankCode; }
    public String getAgencyCode()              { return agencyCode; }
    public String getAccountNumber()           { return accountNumber; }
    public String getAccountType()             { return accountType; }
    public String getOperationCode()           { return operationCode; }
    public String getSiafiOrderNumber()        { return siafiOrderNumber; }
    public String getSiafiCommitmentNumber()   { return siafiCommitmentNumber; }
    public String getManagementUnitCode()      { return managementUnitCode; }
    public String getManagementCode()          { return managementCode; }
    public String getSiafiIntegrationStatus()  { return siafiIntegrationStatus; }
    public LocalDate getReconciliationDate()   { return reconciliationDate; }
    public String getReconciliationStatus()    { return reconciliationStatus; }
    public BigDecimal getReconciledAmount()    { return reconciledAmount; }
    public String getBankReturnCode()          { return bankReturnCode; }
    public String getBankReturnDescription()   { return bankReturnDescription; }
    public String getRemittanceFileHash()      { return remittanceFileHash; }
    public String getReturnFileHash()          { return returnFileHash; }
    public LocalDate getCreatedDate()          { return createdDate; }
    public String getCreatedBy()               { return createdBy; }
    public LocalDate getLastUpdatedDate()      { return lastUpdatedDate; }
    public String getLastUpdatedBy()           { return lastUpdatedBy; }

    // ── MÉTODOS DE DOMÍNIO ────────────────────────────────────────────────────

    /** Adiciona um desconto ao grupo periódico (máx 8 — restrição original do Adabas PE). */
    public void addDeduction(DeductionEntity deduction) {
        if (deductions.size() >= 8) {
            throw new IllegalStateException(
                "Maximum 8 deductions per payment (Adabas PE constraint)");
        }
        deductions.add(deduction);
        deduction.setPayment(this);
    }

    /** Atualiza o status do pagamento (validação de transição deve ficar no Service). */
    public void updateStatus(PaymentStatus newStatus, LocalDate date, String updatedBy) {
        this.status = newStatus;
        this.lastUpdatedDate = date;
        this.lastUpdatedBy = updatedBy;
    }
}
