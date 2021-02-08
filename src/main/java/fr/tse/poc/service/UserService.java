package fr.tse.poc.service;

import fr.tse.poc.domain.User;

public interface UserService {
    Long getManagerId(User user) throws Exception;
}
