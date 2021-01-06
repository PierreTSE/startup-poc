package fr.tse.poc.authentication;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
@NoArgsConstructor
public class AuthenticableUser {
    @Id
    private String username;

    private String password;

    private Role role;

    private Long foreignID;

    public AuthenticableUser(String username, String password, Role role, Long foreignID) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.foreignID = foreignID;
    }
}
