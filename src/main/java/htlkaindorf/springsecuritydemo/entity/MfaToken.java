package htlkaindorf.springsecuritydemo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MfaToken {

    @Id
    private String token;

    private Integer oneTimePassword;

}
