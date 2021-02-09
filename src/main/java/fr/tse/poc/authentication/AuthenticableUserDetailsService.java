package fr.tse.poc.authentication;

import fr.tse.poc.domain.People;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticableUserDetailsService implements UserDetailsService {
    @Autowired
    private AuthenticableUserRepository authenticableUserRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final AuthenticableUser user = authenticableUserRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new AuthenticableUserDetails(user);
    }

    public AuthenticableUser addAuthenticableUser(People people, Role role, String password, boolean hashPassword) {
        return authenticableUserRepository.save(new AuthenticableUser(
                people.getFirstname(),
                hashPassword ? bCryptPasswordEncoder.encode(password) : password,
                role,
                people.getId()));
    }
}
