package fr.tse.poc.domain;

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
public class Admin extends People {

    public Admin(String firstname, String lastname) {
        super(firstname, lastname);
    }
    public Admin(User user) {
    	this.setFirstname(user.getFirstname());
    	this.setLastname(user.getLastname());
    }
}