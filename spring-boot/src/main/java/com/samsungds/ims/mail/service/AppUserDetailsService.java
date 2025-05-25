package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.model.AppUser;
import com.samsungds.ims.mail.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // User 엔티티의 roles(Set<String>)를 Spring Security의 GrantedAuthority 리스트로 변환
        Collection<? extends GrantedAuthority> authorities = appUser.getAppRoles().stream()
                .map(appRole -> (GrantedAuthority) appRole)
                .collect(Collectors.toList());

        // Spring Security의 UserDetails 객체 생성
        return new org.springframework.security.core.userdetails.User(
                appUser.getUsername(),
                appUser.getPassword(),
                authorities
        );
    }
}