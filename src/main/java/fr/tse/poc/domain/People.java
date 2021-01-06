package fr.tse.poc.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
@NoArgsConstructor
public class People {
    private @Id @GeneratedValue Long id;

    private String firstname;

    private String lastname;

    public People(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }
}