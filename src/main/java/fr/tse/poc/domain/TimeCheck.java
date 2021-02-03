package fr.tse.poc.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
public class TimeCheck {
    private @Id @GeneratedValue Long id;

    private float time;

    @ManyToOne
    @JsonIgnoreProperties("timeCheck")
    private User user;

    @ManyToOne
    @JsonIgnoreProperties("timeCheck")
    private Project project;
}