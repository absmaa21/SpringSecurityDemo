package htlkaindorf.springsecuritydemo.repositories;

import htlkaindorf.springsecuritydemo.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken getVerificationTokenByToken(String token);

}
