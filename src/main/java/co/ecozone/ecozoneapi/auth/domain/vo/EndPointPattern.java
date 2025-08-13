package co.ecozone.ecozoneapi.auth.domain.vo;

/**
 * 엔드포인트 패턴(e.g., /api/**)을 표현하는 값 객체
 * 생성 시 공백/형식 등을 검증하고 불변성을 보장
 * 참고: 클래스/파일명을 EndpointPattern으로 통일해 사용하는 것을 권장
 * @since 2025-08-13
 * @author jeongdayeon
 */
public final class EndPointPattern {
    private final String value;
    public EndPointPattern(String value) {
        if (value == null || value.isBlank() || value.contains(" ")) {
            throw new IllegalArgumentException("Invalid endpoint pattern: " + value);
        }
        this.value = value;
    }
    public String value() { return value; }
    @Override public boolean equals(Object o){ return o instanceof EndPointPattern ep && ep.value.equals(this.value); }
    @Override public int hashCode(){ return value.hashCode(); }
}