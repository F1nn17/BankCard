package com.shiraku.bankcard.service;

import com.shiraku.bankcard.model.Status;
import com.shiraku.bankcard.model.dto.CardCreateRequest;
import com.shiraku.bankcard.model.dto.TransferRequest;
import com.shiraku.bankcard.model.entity.Card;
import com.shiraku.bankcard.model.entity.User;
import com.shiraku.bankcard.repository.CardRepository;
import com.shiraku.bankcard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CipherService cipherService;

    @InjectMocks
    private CardService cardService;

    private final UUID cardId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final String email = "test@example.com";

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setEmail(email);

        card = new Card();
        card.setId(cardId);
        card.setOwnerId(userId);
        card.setStatus(Status.ACTIVE);
        card.setBalance(BigDecimal.valueOf(100));
    }

    @Test
    void getCardBalance_shouldReturnBalance_whenUserOwnsCard() {
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        BigDecimal balance = cardService.getCardBalance(cardId, email);

        assertEquals(BigDecimal.valueOf(100), balance);
    }

    @Test
    void getCardBalance_shouldThrowAccessDenied_whenUserNotOwner() {
        card.setOwnerId(UUID.randomUUID());
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.getCardBalance(cardId, email));
    }

    @Test
    void transfer_shouldMoveMoneyBetweenCards() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setOwnerId(userId);
        fromCard.setStatus(Status.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(100));

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setOwnerId(userId);
        toCard.setStatus(Status.ACTIVE);
        toCard.setBalance(BigDecimal.ZERO);

        TransferRequest request = new TransferRequest();
        request.setFromCardId(fromCardId);
        request.setToCardId(toCardId);
        request.setAmount(BigDecimal.valueOf(50));

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        Mockito.when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        cardService.transfer(request, email);

        assertEquals(BigDecimal.valueOf(50), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(50), toCard.getBalance());

        Mockito.verify(cardRepository).save(fromCard);
        Mockito.verify(cardRepository).save(toCard);
    }

    @Test
    void blockCardByUser_shouldBlockCard_whenUserIsOwner() {
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.blockCardByUser(cardId, email);

        assertEquals(Status.BLOCKED, card.getStatus());
        Mockito.verify(cardRepository).save(card);
    }

    @Test
    void createCard_shouldCreateCardSuccessfully() {
        String plainCard = "1234567890123456";
        String encryptedCard = "enc123";

        CardCreateRequest request = new CardCreateRequest();
        request.setOwner(email);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(cipherService.encrypt(Mockito.anyString())).thenReturn(encryptedCard);
        Mockito.when(cardRepository.save(Mockito.any(Card.class))).thenAnswer(i -> i.getArgument(0));

        Card created = cardService.createCard(request);

        assertNotNull(created.getNumber());
        assertEquals(userId, created.getOwnerId());
        assertEquals(Status.ACTIVE, created.getStatus());
    }
}

