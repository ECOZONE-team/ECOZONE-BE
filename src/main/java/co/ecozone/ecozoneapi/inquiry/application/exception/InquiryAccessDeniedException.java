package co.ecozone.ecozoneapi.inquiry.application.exception;

public class InquiryAccessDeniedException extends RuntimeException {
    public InquiryAccessDeniedException(String msg) { super(msg); }
}