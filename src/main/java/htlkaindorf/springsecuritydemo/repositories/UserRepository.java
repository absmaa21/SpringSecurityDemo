package htlkaindorf.springsecuritydemo.repositories;

import htlkaindorf.springsecuritydemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.enabled = false")
    List<User> findUsersByUnverified();

}
