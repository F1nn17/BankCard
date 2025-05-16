package com.shiraku.bankcard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiraku.bankcard.model.Role;
import com.shiraku.bankcard.model.Status;
import com.shiraku.bankcard.model.dto.AdminCardDto;
import com.shiraku.bankcard.model.dto.CardCreateRequest;
import com.shiraku.bankcard.model.dto.TransferRequest;
import com.shiraku.bankcard.model.dto.UserCardDto;
import com.shiraku.bankcard.model.entity.Card;
import com.shiraku.bankcard.model.entity.User;
import com.shiraku.bankcard.service.CardService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({JWTUtils.class})
@WithMockUser(username = "user@example.com", roles = {"USER"})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    private User user;
    private CardCreateRequest cardCreateRequest;
    private TransferRequest transferRequest;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setRole(Role.USER);

        cardCreateRequest = new CardCreateRequest();
        cardCreateRequest.setOwner(user.getEmail());

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(UUID.randomUUID());
        transferRequest.setToCardId(UUID.randomUUID());
        transferRequest.setAmount(BigDecimal.valueOf(500.0));
    }

    @Test
    public void getUserCards_shouldReturnCards() throws Exception {
        List<Card> cards = Arrays.asList(
                new Card(UUID.randomUUID(), "Card 1", Status.ACTIVE),
                new Card(UUID.randomUUID(), "Card 2", Status.BLOCKED)
        );

        List<UserCardDto> userCardDtos = cards.stream()
                .map(card -> new UserCardDto(card.getId(), card.getNumber(), card.getExpiryDate(),
                        card.getStatus(), card.getBalance()))
                .collect(Collectors.toList());

        Mockito.when(cardService.getUserCards(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(new PageImpl<>(userCardDtos));

        mockMvc.perform(get("/api/card/my_cards")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    public void getBalance_shouldReturnBalance() throws Exception {
        UUID cardId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(1000.0);

        // Мокаем возвращаемое значение
        Mockito.when(cardService.getCardBalance(cardId, user.getEmail()))
                .thenReturn(balance);

        // Выполняем запрос с поддельным пользователем
        mockMvc.perform(get("/api/card/balance/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.0"));
    }


    @Test
    public void transferBetweenCards_shouldReturnSuccess() throws Exception {
        Mockito.doNothing().when(cardService).transfer(Mockito.any(), Mockito.anyString());

        mockMvc.perform(post("/api/card/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Перевод выполнен"));
    }

    @Test
    public void blockUserCard_shouldReturnSuccess() throws Exception {
        UUID cardId = UUID.randomUUID();

        Mockito.doNothing().when(cardService).blockCardByUser(Mockito.any(), Mockito.anyString());

        mockMvc.perform(post("/api/card/block/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Карта заблокирована"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void createCard_shouldReturnSuccess() throws Exception {
        Card createdCard = new Card(UUID.randomUUID(), "New Card", Status.ACTIVE);

        Mockito.when(cardService.createCard(Mockito.any()))
                .thenReturn(createdCard);

        mockMvc.perform(post("/api/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCard.getId().toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void blockCardByAdmin_shouldReturnSuccess() throws Exception {
        UUID cardId = UUID.randomUUID();

        Mockito.doNothing().when(cardService).blockCardByAdmin(Mockito.any());

        mockMvc.perform(post("/api/card/block-admin/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Карта заблокирована админом"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void activateCard_shouldReturnSuccess() throws Exception {
        UUID cardId = UUID.randomUUID();

        Mockito.doNothing().when(cardService).activateCard(Mockito.any());

        mockMvc.perform(post("/api/card/activate/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Карта активирована"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteCard_shouldReturnSuccess() throws Exception {
        UUID cardId = UUID.randomUUID();

        Mockito.doNothing().when(cardService).deleteCard(Mockito.any());

        mockMvc.perform(delete("/api/card/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Карта удалена"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void getAllCards_shouldReturnCards() throws Exception {
        List<Card> cards = Arrays.asList(
                new Card(UUID.randomUUID(), "Card 1", Status.ACTIVE),
                new Card(UUID.randomUUID(), "Card 2", Status.BLOCKED)
        );

        List<AdminCardDto> adminCardDtos = cards.stream()
                .map(card -> new AdminCardDto(card.getId(), card.getNumber(), card.getExpiryDate(),
                        card.getStatus(), card.getOwnerId()))
                .collect(Collectors.toList());

        Mockito.when(cardService.getAllCards(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(new PageImpl<>(adminCardDtos));

        mockMvc.perform(get("/api/card/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].status").value("BLOCKED"));
    }

}
