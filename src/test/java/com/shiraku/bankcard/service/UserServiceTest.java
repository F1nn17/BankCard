package com.shiraku.bankcard.service;

import com.shiraku.bankcard.model.Role;
import com.shiraku.bankcard.model.dto.RaisingRequest;
import com.shiraku.bankcard.model.dto.UserRequest;
import com.shiraku.bankcard.model.entity.User;
import com.shiraku.bankcard.repository.UserRepository;
import com.shiraku.bankcard.utils.JWTUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public final class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testSaveUser() {
        UserRequest request = new UserRequest("user@example.com", "pass");
        User savedUser = new User();
        savedUser.setEmail("user@example.com");

        Mockito.when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

        String result = userService.save(request);

        assertEquals("user@example.com", result);
        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    void testLogin_Success() {
        UserRequest request = new UserRequest("user@example.com", "rawPass");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPassword("encodedPass");
        user.setRole(Role.USER);

        Mockito.when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        Mockito.when(jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole())).thenReturn("mockToken");

        String token = userService.login(request);

        assertEquals("mockToken", token);
    }

    @Test
    void testLogin_InvalidPassword() {
        UserRequest request = new UserRequest("user@example.com", "wrong");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPass");

        Mockito.when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.login(request));
    }

    @Test
    void testLogin_UserNotFound() {
        UserRequest request = new UserRequest("notfound@example.com", "pass");

        Mockito.when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.login(request));
    }

    @Test
    void testRaising_Success() {
        RaisingRequest request = new RaisingRequest("admin@example.com", "pass", "_0_1_2_3_4_5_6_7_");

        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("encodedPass");
        user.setRole(Role.USER);

        Mockito.when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("pass", "encodedPass")).thenReturn(true);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        User updated = userService.raising(request);

        assertEquals(Role.ADMIN, updated.getRole());
    }

    @Test
    void testDeleteUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEmail("del@example.com");

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));

        String email = userService.deleteUser(id);

        assertEquals("del@example.com", email);
        Mockito.verify(userRepository).delete(user);
    }

    @Test
    void testIncorrectPassword() {
        Mockito.when(passwordEncoder.matches("raw", "encoded")).thenReturn(false);
        assertTrue(userService.incorrectPassword("raw", "encoded"));
    }

    @Test
    void testFindAll() {
        List<User> users = List.of(new User(), new User());
        Mockito.when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void testFindByEmail_UserFound() {
        User user = new User();
        user.setEmail("user@example.com");

        Mockito.when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("user@example.com");

        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void testFindByEmail_UserNotFound() {
        Mockito.when(userRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());

        User result = userService.findByEmail("none@example.com");

        assertNull(result);
    }
}
