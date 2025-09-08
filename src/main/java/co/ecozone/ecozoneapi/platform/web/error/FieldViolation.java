package co.ecozone.ecozoneapi.platform.web.error;

/**
 * 단일 필드 검증 실패 정보를 담는 VO
 *
 * - Bean Validation 및 바인딩 오류를 ErrorResponse.errors로 매핑할 때 사용
 * - (field, value, reason) 구조
 *
 * @author jeongdayeon
 * @since   2025-09-08
 */
public record FieldViolation(String field, Object value, String reason) {
}
