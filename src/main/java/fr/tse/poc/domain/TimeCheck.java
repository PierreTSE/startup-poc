package fr.tse.poc.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data
@Entity
public class TimeCheck {
    private @Id @GeneratedValue Long id;

    private float time;

    @ManyToOne
    private User user;

    @ManyToOne
    private Project project;
}