package com.shiraku.bankcard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiraku.bankcard.model.Role;
import com.shiraku.bankcard.model.dto.UserRequest;
import com.shiraku.bankcard.model.entity.User;
import com.shiraku.bankcard.service.UserService;
import com.shiraku.bankcard.utils.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({JWTUtils.class})
@WithMockUser(username = "user")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JWTUtils jwtUtils;

    private UserRequest validRequest;
    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("pass123567");
        user.setRole(Role.USER);

        validRequest = new UserRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("pass123567");
    }

    @Test
    void register_successful() throws Exception {
        Mockito.when(userService.findByEmail(validRequest.getEmail())).thenReturn(null);
        Mockito.when(userService.save(Mockito.any(UserRequest.class))).thenReturn(String.valueOf(user));

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Success registering: " + user)));
    }

    @Test
    void register_emailAlreadyExists() throws Exception {
        Mockito.when(userService.findByEmail(validRequest.getEmail())).thenReturn(user);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Email Already Exist")));
    }

    @Test
    void register_missingFields() throws Exception {
        UserRequest invalidRequest = new UserRequest();

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_successful() throws Exception {
        String token = "mock-token";
        Mockito.when(userService.findByEmail(validRequest.getEmail())).thenReturn(user);
        Mockito.when(userService.login(validRequest)).thenReturn(token);

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void login_userNotFound() throws Exception {
        Mockito.when(userService.findByEmail(validRequest.getEmail())).thenReturn(null);

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_invalidCredentials() throws Exception {
        validRequest.setEmail(null);
        Mockito.when(userService.login(validRequest)).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_missingFields() throws Exception {
        UserRequest invalidRequest = new UserRequest();

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }
}

