package fr.tse.poc.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@Data
@MappedSuperclass
@NoArgsConstructor
public class People {
    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @NotNull
    private String firstname;
    @NotNull
    private String lastname;

    public People(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }
}