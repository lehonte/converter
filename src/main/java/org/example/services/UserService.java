package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserDto;
import org.example.entities.User;
import org.example.repositiries.UserRepository;
import org.example.security.JpaUserDetailService;
import org.example.security.JwtToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtToken jwtToken;
    private final JpaUserDetailService jpaUserDetailService;

    public UserDto login(String username, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь "+username+" не найден"));

        String token = jwtToken.generateToken(jpaUserDetailService.loadUserByUsername(username));
        return UserDto.builder()
                .username(user.getUsername())
                .role(user.getRole())
                .token(token)
                .build();
    }
}

