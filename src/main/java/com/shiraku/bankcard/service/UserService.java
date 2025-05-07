package com.shiraku.bankcard.service;

import com.shiraku.bankcard.model.Role;
import com.shiraku.bankcard.model.dto.RaisingRequest;
import com.shiraku.bankcard.model.dto.UserRequest;
import com.shiraku.bankcard.model.entity.User;
import com.shiraku.bankcard.repository.UserRepository;
import com.shiraku.bankcard.utils.JWTUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Tag(name = "Управление пользователями", description = "Класс сервиса для управления пользователями")
public class UserService {
    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, JWTUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String save(UserRequest userRequest) {
        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setCards(new ArrayList<>());
        user.setRole(Role.USER);
        return userRepository.save(user).getEmail();
    }

    public String login(UserRequest userRequest) {
        if (userRequest == null || userRequest.getEmail() == null || userRequest.getPassword() == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = findByEmail(userRequest.getEmail());

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        if (incorrectPassword(userRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());

        if (token == null) {
            throw new RuntimeException("Token generation failed");
        }

        return token;
    }


    public User raising(RaisingRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(incorrectPassword(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

    public String deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userRepository.delete(user);
        return user.getEmail();
    }

    public boolean incorrectPassword(String inputPassword, String confirmPassword) {
        return !passwordEncoder.matches(inputPassword, confirmPassword);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
