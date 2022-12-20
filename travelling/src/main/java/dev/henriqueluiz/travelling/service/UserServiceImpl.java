package dev.henriqueluiz.travelling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.henriqueluiz.travelling.model.AppRole;
import dev.henriqueluiz.travelling.model.AppUser;
import dev.henriqueluiz.travelling.repository.RoleRepo;
import dev.henriqueluiz.travelling.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private final PasswordEncoder encoder;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    
    @Override
    public AppRole saveRole(AppRole role) {
        LOG.debug("Saving new role: '{}'", role.getName());
        return roleRepo.save(role);
    }

    @Override
    public AppUser saveUser(AppUser user) {
        LOG.debug("Saving new user: '{}'", user.getEmail());
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepo.save(user);
    }
    
    @Override
    public void addRolesToUser(String roleName, String email) {
        LOG.debug("Adding '{}' to '{}'", roleName, email);

        AppRole role = roleRepo.findByName(roleName).orElseThrow(() -> {
            LOG.error("Role not found: '{}'", roleName);
            return new EntityNotFoundException(String.format("Role not found: '%s'", roleName));
        });

        AppUser user = userRepo.findByEmail(email).orElseThrow(() -> {
            LOG.error("User not found: '{}'", email);
            return new EntityNotFoundException(String.format("User not found: '%s'", email));
        });

        user.getAuthorities().add(role);
        LOG.debug("Role was been added successfuly");
    }
    
    @Override
    public AppUser getUserByEmail(String email) {
        LOG.debug("Fetching user: '{}'", email);
        return userRepo.findByEmail(email).orElseThrow(() -> {
            LOG.error("User not found: '{}'", email);
            return new EntityNotFoundException(String.format("User not found: '%s'", email));
        });
    }
}