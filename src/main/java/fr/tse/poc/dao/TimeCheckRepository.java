package fr.tse.poc.dao;

import fr.tse.poc.domain.TimeCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeCheckRepository extends JpaRepository<TimeCheck, Long> {
}
