package fr.tse.poc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(indexes = {
        @Index(name = "project_manager_index", columnList = "manager_id")
})
public class Project {
    @EqualsAndHashCode.Include
    @ToString.Include
    @Id @GeneratedValue
    private Long id;

    @ToString.Include
    @NotNull @NotEmpty
    private String name;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JsonIgnore
    private Manager manager;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<TimeCheck> timeChecks = new HashSet<>();

    public Project(String name) {
        this.name = name;
    }

    // manager field managed by its accessors

    public void addUser(User user) {
        users.add(user);
        user.getProjects().add(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.getProjects().remove(this);
    }

    public void addTimeCheck(TimeCheck timeCheck) {
        timeChecks.add(timeCheck);
        timeCheck.setProject(this);
    }

    public void removeTimeCheck(TimeCheck timeCheck) {
        timeChecks.remove(timeCheck);
        timeCheck.setProject(null);
    }
}