package fr.tse.poc.service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.User;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
    private UserRepository userRepository;
    @Autowired
    private ManagerRepository managerRepository;
	@Override
	@Transactional(readOnly = true)
	public Long getManagerId(User user) throws Exception {
		User foundUser=this.userRepository.findById(user.getId()).orElse(null);
		// force initialization
		Hibernate.initialize(foundUser.getManager());
		return foundUser.getManager().getId();
	}

}
