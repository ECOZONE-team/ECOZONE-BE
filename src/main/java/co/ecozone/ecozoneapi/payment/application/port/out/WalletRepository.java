package co.ecozone.ecozoneapi.payment.application.port.out;

import co.ecozone.ecozoneapi.payment.domain.model.WalletEntry;

public interface WalletRepository {
    void apply(WalletEntry e);
}