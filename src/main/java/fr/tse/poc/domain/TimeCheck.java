package fr.tse.poc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(indexes = {
        @Index(name = "timecheck_user_index", columnList = "user_id"),
        @Index(name = "timecheck_project_index", columnList = "project_id")
}
)
public class TimeCheck {
    @EqualsAndHashCode.Include
    @Id @GeneratedValue
    private Long id;

    @NotNull @Min(0)
    private double time;

    @NotNull
    @ManyToOne
    @JsonIgnore
    private User user;

    @NotNull
    @ManyToOne
    @JsonIgnore
    private Project project;

    public TimeCheck(double time) {
        // todo enforce multiple of 0.125 (use integral type ?)
        this.time = time;
    }

    // user field managed by its accessors

    // project field managed by its accessors
}