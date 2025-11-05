package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.WalletEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataWalletRepository extends JpaRepository<WalletEntryJpaEntity, Long> {
    // 최근 잔액 조회용(단순 접근): 계정별 최신 엔트리
    Optional<WalletEntryJpaEntity> findTopByAccountIdOrderByIdDesc(String accountId);
}

