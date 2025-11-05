package co.ecozone.ecozoneapi.payment.application.port.in;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;

public interface CreateOrderUseCase {
    Long create(String orderId, long amount, UserId userId);
}
