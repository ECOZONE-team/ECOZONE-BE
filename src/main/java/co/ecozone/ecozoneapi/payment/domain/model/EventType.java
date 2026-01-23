package co.ecozone.ecozoneapi.payment.domain.model;

public enum EventType {
    PAYMENT_CONFIRMED,    // 결제 승인 완료
    PAYMENT_CANCELED,     // 결제 취소 완료
    PAYMENT_FAILED,       // 결제 실패
    VIRTUAL_ACCOUNT_ISSUED, // 가상계좌 발급
    VIRTUAL_ACCOUNT_DEPOSITED // 가상계좌 입금
}
