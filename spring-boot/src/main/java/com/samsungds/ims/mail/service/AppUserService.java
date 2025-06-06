package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.model.AppRole;
import com.samsungds.ims.mail.model.AppUser;
import com.samsungds.ims.mail.repository.AppRoleRepository;
import com.samsungds.ims.mail.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUser saveUser(String username, String password, Set<String> roleNames, String email) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);

        Set<AppRole> roles = new HashSet<>();
        for (String roleName : roleNames) {
            AppRole role = appRoleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        AppRole appRole = new AppRole();
                        appRole.setName(roleName);
                        return appRoleRepository.save(appRole);
                    });
            roles.add(role);
        }
        user.setAppRoles(roles);

        return appUserRepository.save(user);
    }

    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }
}