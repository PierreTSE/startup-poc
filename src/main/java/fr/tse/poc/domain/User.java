package fr.tse.poc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(indexes = {
        @Index(name = "user_manager_index", columnList = "manager_id")
})
public class User extends People {
    @NotNull
    @ManyToOne
    @JsonIgnore
    private Manager manager;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();
    
    
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<TimeCheck> timeChecks = new HashSet<>();

    public User(String firstname, String lastname) {
        super(firstname, lastname);
    }

    public User(String firstname, String lastname, @NotNull Manager manager) {
        super(firstname, lastname);
        this.manager = manager;
    }

    // manager field managed by its accessors

    public void addProject(Project project) {
        projects.add(project);
        project.getUsers().add(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.getUsers().remove(this);
    }

    public void addTimeCheck(TimeCheck timeCheck) {
        timeChecks.add(timeCheck);
        timeCheck.setUser(this);
    }

    public void removeTimeCheck(TimeCheck timeCheck) {
        timeChecks.remove(timeCheck);
        timeCheck.setUser(null);
    }
}
