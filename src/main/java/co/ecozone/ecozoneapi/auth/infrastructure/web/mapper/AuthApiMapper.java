package co.ecozone.ecozoneapi.auth.infrastructure.web.mapper;

import co.ecozone.ecozoneapi.auth.application.command.LoginCommand;
import co.ecozone.ecozoneapi.auth.application.command.SignUpCommand;
import co.ecozone.ecozoneapi.auth.application.dto.Tokens;
import co.ecozone.ecozoneapi.auth.infrastructure.web.request.LoginRequest;
import co.ecozone.ecozoneapi.auth.infrastructure.web.request.SignUpRequest;
import co.ecozone.ecozoneapi.auth.infrastructure.web.response.TokenResponse;
import co.ecozone.ecozoneapi.common.config.MapStructCentralConfig;
import org.mapstruct.Mapper;

/**
 * 웹 어댑터 전용 매퍼(MapStruct/수동 매핑)
 * - API DTO ↔ 애플리케이션 커맨드 간 변환 책임
 * - 컨트롤러에서 프레임워크 의존을 도메인으로 누수시키지 않도록 중간 어댑터 역할 수행
 * @since 2025-08-23
 * @author jeongdayeon
 */

@Mapper(config = MapStructCentralConfig.class)
public interface AuthApiMapper {
    SignUpCommand toCommand(SignUpRequest req);
    LoginCommand toCommand(LoginRequest req);
    TokenResponse toResponse(Tokens tokens);
}
