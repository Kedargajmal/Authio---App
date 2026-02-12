package com.springSecurity.authify.service;

import com.springSecurity.authify.entity.UserEntity;
import com.springSecurity.authify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AppUserdetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found for email: " + email));

        return new User(existingUser.getEmail(), existingUser.getPassword(), new ArrayList<>());
//            .withUsername(existingUser.getEmail())
//                .password(existingUser.getPassword())
//                .authorities("USER")
//                .accountLocked(false)
//                .disabled(false)
//                .build();
    }


}
