package fr.tse.poc.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
public class Project {
    private @Id @GeneratedValue Long id;

    private String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JsonIgnoreProperties("project")
    private Manager manager;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("project")
    private Set<User> users = new HashSet<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("project")
    private Set<TimeCheck> timeChecks = new HashSet<>();

    public Project(String name, Manager manager) {
        this.name = name;
        this.manager = manager;
    }
    
}