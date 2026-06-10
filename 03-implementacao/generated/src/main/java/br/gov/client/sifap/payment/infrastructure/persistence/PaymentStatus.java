package br.gov.client.sifap.payment.infrastructure.persistence;

/**
 * Status do pagamento mapeado do campo DA / SIT-PAGAMENTO (A1) do DDM PAGAMENTO.
 *
 * <p>Fonte: {@code 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm} — campo DA</p>
 *
 * <p>Transições válidas (regra de negócio — confirmar com SPECIFICATION.md):
 * <pre>
 *   PENDING → GENERATED → ISSUED → CONFIRMED
 *                                 → RETURNED
 *             → CANCELLED
 *   CONFIRMED → REPROCESSED → GENERATED
 * </pre>
 * </p>
 */
public enum PaymentStatus {

    /** P — Pendente de processamento */
    PENDING,

    /** G — Gerado pelo batch */
    GENERATED,

    /** E — Emitido / enviado ao banco */
    ISSUED,

    /** C — Confirmado pelo retorno bancário */
    CONFIRMED,

    /** D — Devolvido pelo banco */
    RETURNED,

    /** X — Cancelado */
    CANCELLED,

    /** R — Reprocessado */
    REPROCESSED
}
