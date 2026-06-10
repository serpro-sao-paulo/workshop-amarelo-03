package br.gov.client.sifap.payment.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidade JPA para o grupo periódico GRP-DESCONTO do DDM PAGAMENTO.
 *
 * <p>Fonte legada: {@code 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm}
 * campos CA–CG (PE, max 8 ocorrências).</p>
 *
 * <p>Adabas PE (Periodic Group) → tabela relacional separada com FK para {@link PaymentEntity}.
 * Cada linha representa uma ocorrência do PE.</p>
 *
 * <p>Tipos de desconto (DDM: CB / TIPO-DESCONTO A3):
 * <ul>
 *   <li>IR  — Imposto de Renda</li>
 *   <li>JD  — Judicial</li>
 *   <li>CS  — INSS/Contribuição Social</li>
 *   <li>PA  — Pensão Alimentícia</li>
 *   <li>EM  — Empréstimo Consignado</li>
 *   <li>TX  — Taxa Administrativa</li>
 *   <li>OU  — Outros</li>
 *   <li>EX  — Extrajudicial</li>
 * </ul>
 * </p>
 */
@Entity
@Table(
    name = "payment_deduction",
    indexes = {
        @Index(name = "idx_deduction_payment_id", columnList = "payment_id"),
        @Index(name = "idx_deduction_type",       columnList = "discount_type")
    }
)
public class DeductionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deduction_seq")
    @SequenceGenerator(name = "deduction_seq", sequenceName = "payment_deduction_seq", allocationSize = 50)
    @Column(name = "id")
    private Long id;

    /**
     * Referência ao pagamento pai.
     * FK para {@link PaymentEntity}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, updatable = false)
    private PaymentEntity payment;

    /**
     * Posição da ocorrência no grupo PE original (1-based, max 8).
     * Preserva a ordem do Adabas para auditoria e equivalência.
     */
    @Column(name = "occurrence_order", nullable = false)
    private Integer occurrenceOrder;

    /**
     * Tipo de desconto.
     * DDM: CB / TIPO-DESCONTO (A3): IR/JD/CS/PA/EM/TX/OU/EX
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 3)
    private DiscountType discountType;

    /**
     * Valor do desconto.
     * DDM: CC / VLR-DESCONTO (N7.2)
     */
    @Column(name = "amount", nullable = false, precision = 9, scale = 2)
    private BigDecimal amount;

    /**
     * Percentual aplicado.
     * DDM: CD / PCT-DESCONTO (N3.2)
     */
    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    /**
     * Número do processo judicial (apenas para tipo JD).
     * DDM: CE / NUM-PROCESSO (A20)
     */
    @Column(name = "judicial_process_number", length = 20)
    private String judicialProcessNumber;

    /**
     * Data de início do desconto.
     * DDM: CF / DT-INICIO-DSCT (N8 AAAAMMDD)
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Data de fim do desconto. Null = indefinido (original: 0 no Adabas).
     * DDM: CG / DT-FIM-DSCT (N8 AAAAMMDD — 0=INDEFINIDO)
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    protected DeductionEntity() {
        // JPA
    }

    public DeductionEntity(Integer occurrenceOrder, DiscountType discountType,
                           BigDecimal amount, BigDecimal percentage) {
        this.occurrenceOrder = occurrenceOrder;
        this.discountType = discountType;
        this.amount = amount;
        this.percentage = percentage;
    }

    // ── GETTERS ───────────────────────────────────────────────────────────────

    public Long getId()                        { return id; }
    public PaymentEntity getPayment()          { return payment; }
    public Integer getOccurrenceOrder()        { return occurrenceOrder; }
    public DiscountType getDiscountType()      { return discountType; }
    public BigDecimal getAmount()              { return amount; }
    public BigDecimal getPercentage()          { return percentage; }
    public String getJudicialProcessNumber()   { return judicialProcessNumber; }
    public LocalDate getStartDate()            { return startDate; }
    public LocalDate getEndDate()              { return endDate; }

    // Setter interno para o relacionamento bidirecional
    void setPayment(PaymentEntity payment)     { this.payment = payment; }

    /** Verifica se este desconto é judicial (sem teto de 30% — REQ-PAY-001). */
    public boolean isJudicial() {
        return DiscountType.JD == this.discountType;
    }
}
