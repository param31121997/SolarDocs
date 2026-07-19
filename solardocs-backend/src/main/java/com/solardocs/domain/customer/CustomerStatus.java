package com.solardocs.domain.customer;

/** Section 2, FR-1.6 lifecycle */
public enum CustomerStatus {
    LEAD,
    QUOTATION_SENT,
    AGREEMENT_SIGNED,
    DOCUMENTS_COLLECTED,
    INSTALLATION_IN_PROGRESS,
    COMMISSIONED,
    SUBSIDY_APPLIED,
    SUBSIDY_RECEIVED,
    CLOSED
}
