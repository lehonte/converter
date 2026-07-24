package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserDto;
import org.example.entities.User;
import org.example.repositories.UserRepository;
import org.example.security.JpaUserDetailService;
import org.example.security.JwtToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        authenticationManager.authenticate(
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

//сразу берет полученный логин пароль из auth, тк authenticationManager уже сгонял в бд
//и больше можно не ходть в бд а брать auth а у меня щас 3 запроса в бд тк не юзаю auth
//User user = (User) auth.getPrincipal();
//
//тоже сразу кладем уже найденного пользователя
//String token = jwtToken.generateToken(user);
//return UserDto.builder().username(user.getUsername()).token(token)...build();
