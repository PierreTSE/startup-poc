package fr.tse.poc.dao;

import fr.tse.poc.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagedRepository extends JpaRepository<User, Long> {
}
