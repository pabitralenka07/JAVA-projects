package com.portfolio.isp.exception;

public class DuplicateInvoiceException extends RuntimeException {
    public DuplicateInvoiceException(String message) {
        super(message);
    }
}
