package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.WalletEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataWalletRepository extends JpaRepository<WalletEntryJpaEntity, Long> {
    // 최근 잔액 조회용(단순 접근): 계정별 최신 엔트리
    Optional<WalletEntryJpaEntity> findTopByAccountIdOrderByIdDesc(String accountId);

    /**
     * N+1 쿼리 해결: 여러 계정의 최신 엔트리를 한 번에 조회
     */
    @Query("""
        SELECT w FROM WalletEntryJpaEntity w 
        WHERE w.id IN (
            SELECT MAX(w2.id) 
            FROM WalletEntryJpaEntity w2 
            WHERE w2.accountId IN :accountIds 
            GROUP BY w2.accountId
        )
        """)
    List<WalletEntryJpaEntity> findLatestByAccountIds(@Param("accountIds") List<String> accountIds);
}

