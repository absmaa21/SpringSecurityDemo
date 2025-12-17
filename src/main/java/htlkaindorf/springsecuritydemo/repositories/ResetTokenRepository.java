package htlkaindorf.springsecuritydemo.repositories;

import htlkaindorf.springsecuritydemo.entity.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    ResetToken findResetTokenByToken(String token);
    
}
