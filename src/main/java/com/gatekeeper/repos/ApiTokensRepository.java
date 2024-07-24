package com.gatekeeper.repos;

import com.gatekeeper.entity.ApiTokens;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author null
 */
@Repository
public interface ApiTokensRepository extends JpaRepository<ApiTokens, Long> {

    Optional<ApiTokens> findByUserTokens(String userTokens);

}
