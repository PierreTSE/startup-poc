package fr.tse.poc.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Manager extends People {
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "manager", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("manager")
    private Set<User> users = new HashSet<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "manager", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("manager")
    private Set<Project> projects = new HashSet<>();

    public Manager(String firstname, String lastname) {
        super(firstname, lastname);
    }
}