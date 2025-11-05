package co.ecozone.ecozoneapi.payment.domain.policy;

/** 승인/취소 시 부가 규칙(수수료, 한도, 금액 검증 등)을 묶는 확장 포인트 */
public interface PaymentPolicy {
    void validateConfirm(String userId, long amount);
    void validateCancel(long amount);
}
