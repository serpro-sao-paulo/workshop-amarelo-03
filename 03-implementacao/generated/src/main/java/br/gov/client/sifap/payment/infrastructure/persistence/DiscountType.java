package br.gov.client.sifap.payment.infrastructure.persistence;

/**
 * Tipos de desconto mapeados do campo CB / TIPO-DESCONTO (A3) do DDM PAGAMENTO.
 *
 * <p>Fonte: {@code 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm} — campo CB</p>
 */
public enum DiscountType {

    /** Imposto de Renda */
    IR,

    /** Desconto Judicial — sem teto de 30% (REQ-PAY-001 / REQ-PAY-002) */
    JD,

    /** Contribuição Social / INSS */
    CS,

    /** Pensão Alimentícia */
    PA,

    /** Empréstimo Consignado */
    EM,

    /** Taxa Administrativa */
    TX,

    /** Outros */
    OU,

    /** Extrajudicial */
    EX
}
