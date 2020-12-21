package fr.tse.poc.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class User extends People {
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    private Manager manager;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    private Project project;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("user")
    private Set<TimeCheck> timeChecks = new HashSet<>();

    public User(String firstname, String lastname) {
        super(firstname, lastname);
    }
}
