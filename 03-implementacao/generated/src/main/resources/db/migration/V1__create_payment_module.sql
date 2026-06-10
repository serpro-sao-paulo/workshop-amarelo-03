-- =============================================================================
-- Flyway Migration: V1__create_payment_module.sql
-- =============================================================================
-- Origem: DDM Adabas PAGAMENTO (FNR 152)
-- Arquivo legado: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
-- Bounded context: payment
-- Criado por: /generate-jpa-from-fdt (stage-builder)
--
-- ATENÇÃO: NUNCA editar esta migration. Criar V2__ para alterações.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Tabela principal: payment
-- Origem: DDM PAGAMENTO FNR 152 (campos AA–IF)
-- Aprox. 180M registros no sistema legado
-- ---------------------------------------------------------------------------
CREATE TABLE payment (

    -- IDENTIFICAÇÃO (DDM: AA–AF)
    id                      BIGINT          NOT NULL,            -- AA: NUM-PAGAMENTO (N15) — sequencial único
    cpf                     CHAR(11)        NOT NULL,            -- AB: NUM-CPF (A11) — CPF sem formatação
    enrollment_number       BIGINT,                              -- AC: NUM-INSCRICAO (N11) — matrícula
    program_code            CHAR(4)         NOT NULL,            -- AD: COD-PROGRAMA (A4)
    reference_month         INTEGER         NOT NULL,            -- AE: ANO-MES-REF (N6 AAAAMM)
    cycle_number            INTEGER,                             -- AF: NUM-CICLO (N6)

    -- VALORES (DDM: BA–BC)
    gross_amount            NUMERIC(11,2)   NOT NULL,            -- BA: VLR-BRUTO (N9.2)
    net_amount              NUMERIC(11,2)   NOT NULL,            -- BB: VLR-LIQUIDO (N9.2)
    total_deduction         NUMERIC(9,2)    NOT NULL DEFAULT 0,  -- BC: VLR-DESCONTO-TOTAL (N7.2)

    -- STATUS E PROCESSAMENTO (DDM: DA–DG)
    status                  VARCHAR(20)     NOT NULL,            -- DA: SIT-PAGAMENTO (A1) — expandido para enum
    generation_date         DATE,                                -- DB: DT-GERACAO (N8)
    generation_time         INTEGER,                             -- DC: HR-GERACAO (N6 HHMMSS)
    emission_date           DATE,                                -- DD: DT-EMISSAO (N8)
    confirmation_date       DATE,                                -- DE: DT-CONFIRMACAO (N8)
    cancellation_date       DATE,                                -- DF: DT-CANCELAMENTO (N8)
    cancellation_reason_code CHAR(3),                           -- DG: MOT-CANCELAMENTO (A3)

    -- DADOS BANCÁRIOS (DDM: EA–EE)
    bank_code               CHAR(3),                             -- EA: COD-BANCO (A3) FEBRABAN
    agency_code             VARCHAR(6),                          -- EB: COD-AGENCIA (A6)
    account_number          VARCHAR(13),                         -- EC: NUM-CONTA (A13)
    account_type            CHAR(1),                             -- ED: TIPO-CONTA (A1) C/P
    operation_code          CHAR(3),                             -- EE: COD-OPERACAO (A3)

    -- INTEGRAÇÃO SIAFI (DDM: FA–FE)
    siafi_order_number      CHAR(12),                            -- FA: NUM-OB-SIAFI (A12)
    siafi_commitment_number CHAR(12),                            -- FB: NUM-NE-SIAFI (A12)
    management_unit_code    CHAR(6),                             -- FC: COD-UG-EMITENTE (A6)
    management_code         CHAR(5),                             -- FD: COD-GESTAO (A5)
    siafi_integration_status CHAR(1),                           -- FE: SIT-INTEG-SIAFI (A1) I/P/E

    -- CONCILIAÇÃO BANCÁRIA (DDM: GA–GE)
    reconciliation_date     DATE,                                -- GA: DT-CONCILIACAO (N8)
    reconciliation_status   CHAR(1),                             -- GB: SIT-CONCILIACAO (A1) C/D/P/N
    reconciled_amount       NUMERIC(11,2),                       -- GC: VLR-CONCILIADO (N9.2)
    bank_return_code        CHAR(2),                             -- GD: COD-RETORNO-BANCO (A2) CNAB 240
    bank_return_description VARCHAR(40),                         -- GE: DES-RETORNO-BANCO (A40)

    -- HASH DE ARQUIVO (DDM: HA–HB)
    remittance_file_hash    CHAR(64),                            -- HA: HASH-ARQ-REMESSA (SHA-256)
    return_file_hash        CHAR(64),                            -- HB: HASH-ARQ-RETORNO (SHA-256)

    -- CONTROLE DE AUDITORIA (DDM: IA–IF)
    created_date            DATE            NOT NULL,            -- IA: DT-INCLUSAO (N8)
    created_time            INTEGER,                             -- IB: HR-INCLUSAO (N6)
    created_by              VARCHAR(8)      NOT NULL DEFAULT 'BATCH', -- IC: USR-INCLUSAO (A8)
    last_updated_date       DATE,                                -- ID: DT-ULT-ALTERACAO (N8)
    last_updated_time       INTEGER,                             -- IE: HR-ULT-ALTERACAO (N6)
    last_updated_by         VARCHAR(8),                          -- IF: USR-ULT-ALTERACAO (A8)

    CONSTRAINT pk_payment PRIMARY KEY (id),

    CONSTRAINT chk_payment_status CHECK (
        status IN ('PENDING','GENERATED','ISSUED','CONFIRMED','RETURNED','CANCELLED','REPROCESSED')
    ),
    CONSTRAINT chk_payment_account_type CHECK (
        account_type IS NULL OR account_type IN ('C','P')
    ),
    CONSTRAINT chk_payment_reconciliation_status CHECK (
        reconciliation_status IS NULL OR reconciliation_status IN ('C','D','P','N')
    ),
    CONSTRAINT chk_payment_siafi_status CHECK (
        siafi_integration_status IS NULL OR siafi_integration_status IN ('I','P','E')
    ),
    CONSTRAINT chk_payment_amounts CHECK (
        gross_amount >= 0 AND net_amount >= 0 AND total_deduction >= 0
    ),
    CONSTRAINT chk_payment_net_amount CHECK (
        net_amount <= gross_amount
    ),
    CONSTRAINT chk_payment_reference_month CHECK (
        reference_month BETWEEN 199801 AND 209912   -- histórico desde 1998
    )
);

-- ---------------------------------------------------------------------------
-- Tabela filha: payment_deduction
-- Origem: GRP-DESCONTO (PE, max 8 ocorrências) — campos CA–CG do DDM
-- ---------------------------------------------------------------------------
CREATE SEQUENCE payment_deduction_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE payment_deduction (

    id                      BIGINT          NOT NULL DEFAULT nextval('payment_deduction_seq'),
    payment_id              BIGINT          NOT NULL,            -- FK para payment
    occurrence_order        SMALLINT        NOT NULL,            -- posição no PE original (1–8)

    -- DDM: CB / TIPO-DESCONTO (A3)
    discount_type           CHAR(3)         NOT NULL,            -- IR/JD/CS/PA/EM/TX/OU/EX

    -- DDM: CC / VLR-DESCONTO (N7.2)
    amount                  NUMERIC(9,2)    NOT NULL,

    -- DDM: CD / PCT-DESCONTO (N3.2)
    percentage              NUMERIC(5,2),

    -- DDM: CE / NUM-PROCESSO (A20) — apenas tipo JD
    judicial_process_number VARCHAR(20),

    -- DDM: CF / DT-INICIO-DSCT (N8 AAAAMMDD)
    start_date              DATE,

    -- DDM: CG / DT-FIM-DSCT (N8 — 0=INDEFINIDO mapeado como NULL)
    end_date                DATE,

    CONSTRAINT pk_payment_deduction PRIMARY KEY (id),

    CONSTRAINT fk_deduction_payment FOREIGN KEY (payment_id)
        REFERENCES payment (id) ON DELETE CASCADE,

    CONSTRAINT chk_deduction_type CHECK (
        discount_type IN ('IR','JD','CS','PA','EM','TX','OU','EX')
    ),
    CONSTRAINT chk_deduction_occurrence CHECK (
        occurrence_order BETWEEN 1 AND 8
    ),
    CONSTRAINT chk_deduction_amount CHECK (
        amount >= 0
    ),
    CONSTRAINT chk_deduction_judicial CHECK (
        discount_type != 'JD' OR judicial_process_number IS NOT NULL
    ),
    CONSTRAINT uq_deduction_payment_occurrence UNIQUE (payment_id, occurrence_order)
);

-- ---------------------------------------------------------------------------
-- Índices — mapeados dos superdescriptors Adabas e padrões de acesso
-- ---------------------------------------------------------------------------

-- S1: CPF + COMPETENCIA (busca principal de beneficiário)
CREATE INDEX idx_payment_cpf_month
    ON payment (cpf, reference_month);

-- S2: PROGRAMA + COMPETENCIA + SITUACAO (relatórios de ciclo)
CREATE INDEX idx_payment_prog_month_status
    ON payment (program_code, reference_month, status);

-- S3: CICLO + SITUACAO (processamento batch)
CREATE INDEX idx_payment_cycle_status
    ON payment (cycle_number, status);

-- DT-GERACAO (DE no DDM — usado em buscas batch)
CREATE INDEX idx_payment_generation_date
    ON payment (generation_date);

-- CPF isolado (consultas por beneficiário sem filtro de mês)
CREATE INDEX idx_payment_cpf
    ON payment (cpf);

-- FK index (payment_deduction)
CREATE INDEX idx_deduction_payment_id
    ON payment_deduction (payment_id);

CREATE INDEX idx_deduction_type
    ON payment_deduction (discount_type);

-- ---------------------------------------------------------------------------
-- Comentários de tabela (documentação no catálogo do banco)
-- ---------------------------------------------------------------------------
COMMENT ON TABLE payment IS
    'Histórico de pagamentos SIFAP. Origem: DDM PAGAMENTO FNR 152 (Natural/Adabas). '
    'Aprox. 180M registros. Não possui política de purge — todos os registros desde 1998.';

COMMENT ON TABLE payment_deduction IS
    'Descontos por pagamento. Origem: GRP-DESCONTO PE (max 8 ocorrências) do DDM PAGAMENTO campos CA-CG.';

COMMENT ON COLUMN payment.cpf IS
    'CPF sem formatação, 11 dígitos. NUNCA logar sem mascaramento.';

COMMENT ON COLUMN payment.reference_month IS
    'Competência no formato YYYYMM (ex: 202604). Mapeado de ANO-MES-REF N6 do Adabas.';

COMMENT ON COLUMN payment_deduction.end_date IS
    'NULL = desconto sem prazo de fim. No Adabas original o valor 0 indicava indefinido.';
