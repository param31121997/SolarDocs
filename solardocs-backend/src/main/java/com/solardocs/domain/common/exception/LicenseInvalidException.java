package com.solardocs.domain.common.exception;

public class LicenseInvalidException extends DomainException {
    public LicenseInvalidException(String message) { super("LICENSE_INVALID", message); }
}
