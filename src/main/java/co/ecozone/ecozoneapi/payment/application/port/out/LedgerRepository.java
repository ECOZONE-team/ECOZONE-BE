package co.ecozone.ecozoneapi.payment.application.port.out;

import co.ecozone.ecozoneapi.payment.domain.model.LedgerEntry;

public interface LedgerRepository {
    void append(LedgerEntry e);
}
