package co.ecozone.ecozoneapi.auth.domain.model;

/**
 * 토큰 검증 실패 사유를 표현하는 기계가독형 에러 코드 열거형
 * 핸들러가 일관된 API 에러 응답(401)을 생성하도록 도움
 * @since 2025-08-13
 * @author jeongdayeon
 */
public enum VerificationError {
    /** 잘못된 JWT 형식: 파싱 실패, 토큰 구조/헤더/페이로드 손상, 필수 클레임(sub 등) 누락 */
    MALFORMED,

    /** 서명 검증 실패: 비밀키 불일치, 토큰 변조, 서명 알고리즘 불일치 등 */
    BAD_SIGNATURE,

    /** 만료(exp) 초과: now > exp + 허용시계오차(skew) 인 경우 */
    EXPIRED,

    /** 아직 유효하지 않음: now < nbf - skew 이거나 iat(발급시각)가 미래인 경우 */
    NOT_BEFORE,

    /** 위 경우들 외의 예외/알 수 없는 오류(라이브러리 예외 등) */
    UNKNOWN
}
