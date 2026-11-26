package com.banking.service;

import com.banking.model.AppUser;
import com.banking.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Optional<AppUser> optionalUser =
                appUserRepository.findByUsername(username);

        if (!optionalUser.isPresent()) {
            throw new UsernameNotFoundException("User not found.");
        }

        AppUser appUser = optionalUser.get();

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .roles(appUser.getRole())
                .disabled(!appUser.isActive())
                .build();
    }
}