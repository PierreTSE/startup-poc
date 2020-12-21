package fr.tse.poc.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Admin extends People {

    public Admin(String firstname, String lastname) {
        super(firstname, lastname);
    }
}