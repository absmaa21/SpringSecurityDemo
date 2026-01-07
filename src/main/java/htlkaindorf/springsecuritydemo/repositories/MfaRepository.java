package htlkaindorf.springsecuritydemo.repositories;

import htlkaindorf.springsecuritydemo.entity.MfaToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfaRepository extends JpaRepository<MfaToken, String> {
}
