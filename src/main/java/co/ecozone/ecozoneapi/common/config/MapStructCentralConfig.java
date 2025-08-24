package co.ecozone.ecozoneapi.common.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct 전역 설정
 * - componentModel = "spring", injectionStrategy, unmappedTargetPolicy 등 중앙화
 * - 각 Mapper에서 @Mapper(config = MapStructCentralConfig.class) 로 재사용
 * - 매핑 규칙 일관성과 빌드 타임 검증 강화를 목적
 * @since 2025-08-22
 * @author jeongdayeon
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface MapStructCentralConfig {}
