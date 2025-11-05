package co.ecozone.ecozoneapi.payment.infrastructure.persistence.request;

public record CreateOrderRequest(String orderId, long amount) {
}

