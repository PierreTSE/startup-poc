package fr.tse.poc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Manager extends People {
    @OneToMany(mappedBy = "manager", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "manager", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

    public Manager(String firstname, String lastname) {
        super(firstname, lastname);
    }

    public void addUser(User user) {
        users.add(user);
        user.setManager(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.setManager(null);
    }

    public void addProject(Project project) {
        projects.add(project);
        project.setManager(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.setManager(null);
    }
}