package com.shiraku.bankcard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiraku.bankcard.model.Role;
import com.shiraku.bankcard.model.dto.RaisingRequest;
import com.shiraku.bankcard.model.entity.User;

import com.shiraku.bankcard.service.CardService;
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
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({JWTUtils.class})
@WithMockUser(username = "admin", roles = {"ADMIN"})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JWTUtils jwtUtils;

    private User user;
    private RaisingRequest validRequest;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setRole(Role.ADMIN);

        validRequest = new RaisingRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("pass123567");
        validRequest.setSecretKey("_0_1_2_3_4_5_6_7_");
    }

    @Test
    public void raising_successful() throws Exception {
        Mockito.when(userService.raising(Mockito.any())).thenReturn(user);

        mockMvc.perform(post("/api/admin/raising")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Success raising: " + user.getEmail())));
    }

    @Test
    public void raising_missingFields() throws Exception {
        RaisingRequest invalid = new RaisingRequest();

        mockMvc.perform(post("/api/admin/raising")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Incorrect input data")));
    }

    @Test
    public void raising_incorrectSecretKey() throws Exception {
        validRequest.setSecretKey("wrong");

        mockMvc.perform(post("/api/admin/raising")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Incorrect")));
    }

    @Test
    public void getAllUsers_shouldReturnList() throws Exception {
        List<User> users = Arrays.asList(
                new User(UUID.randomUUID(), "user1@example.com", Role.USER),
                new User(UUID.randomUUID(), "user2@example.com", Role.ADMIN)
        );

        Mockito.when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].role").value("ADMIN"));
    }

    @Test
    public void deleteUser_shouldReturnSuccess() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(userService.deleteUser(id)).thenReturn("true");

        mockMvc.perform(delete("/api/admin/delete/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Success delete user: true")));
    }
}




