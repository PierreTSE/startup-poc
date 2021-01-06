package fr.tse.poc.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
public class Admin extends People {

    public Admin(String firstname, String lastname) {
        super(firstname, lastname);
    }
}