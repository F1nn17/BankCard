package com.shiraku.bankcard.service;

import com.shiraku.bankcard.model.Status;
import com.shiraku.bankcard.model.dto.AdminCardDto;
import com.shiraku.bankcard.model.dto.CardCreateRequest;
import com.shiraku.bankcard.model.dto.TransferRequest;
import com.shiraku.bankcard.model.dto.UserCardDto;
import com.shiraku.bankcard.model.entity.Card;
import com.shiraku.bankcard.model.entity.User;
import com.shiraku.bankcard.repository.CardRepository;
import com.shiraku.bankcard.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Service
@Tag(name = "Управление картами", description = "Класс сервиса для управления картами пользователя")
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CipherService cipherService;

    public CardService(CardRepository cardRepository, UserRepository userRepository, CipherService cipherService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cipherService = cipherService;
    }

    public Page<UserCardDto> getUserCards(String username, Status status, int page, int size) {
        UUID userId = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"))
                .getId();

        Pageable pageable = PageRequest.of(page, size);

        if(status == null){
            return cardRepository.findByOwnerId(userId, pageable)
                    .map(this::toUserDto);
        }

        return cardRepository.findByOwnerIdAndStatus(userId, status, pageable)
                .map(this::toUserDto);
    }

    public BigDecimal getCardBalance(UUID cardId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!card.getOwnerId().equals(user.getId())) {
            throw new AccessDeniedException("You are not the owner of this card");
        }

        return card.getBalance();
    }

    @Transactional
    public void transfer(TransferRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new EntityNotFoundException("From card not found"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new EntityNotFoundException("To card not found"));

        if (!fromCard.getOwnerId().equals(user.getId()) || !toCard.getOwnerId().equals(user.getId())) {
            throw new AccessDeniedException("Обе карты должны принадлежать вам");
        }

        if (fromCard.getStatus() != Status.ACTIVE || toCard.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Обе карты должны быть активны");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Недостаточно средств");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    public void blockCardByUser(UUID cardId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!card.getOwnerId().equals(user.getId())) {
            throw new AccessDeniedException("Вы не владелец этой карты");
        }

        card.setStatus(Status.BLOCKED);
        cardRepository.save(card);
    }

    public Card createCard(CardCreateRequest request) {
        User user = userRepository.findByEmail(request.getOwner())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String plainCardNumber = generateCardNumber();
        String encryptedCardNumber = encrypt(plainCardNumber);

        Card card = new Card();
        card.setNumber(encryptedCardNumber);
        card.setOwnerId(user.getId());
        card.setExpiryDate(generateExpiryDate());
        card.setStatus(Status.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        return cardRepository.save(card);
    }

    public void activateCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));

        if (card.getStatus() == Status.ACTIVE) {
            throw new IllegalStateException("Карта уже активна");
        }

        card.setStatus(Status.ACTIVE);
        cardRepository.save(card);
    }

    public void blockCardByAdmin(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));

        if (card.getStatus() == Status.BLOCKED) {
            throw new IllegalStateException("Карта уже заблокирована");
        }

        card.setStatus(Status.BLOCKED);
        cardRepository.save(card);
    }

    public Page<AdminCardDto> getAllCards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAll(pageable)
                .map(this::toAdminDto);
    }

    public void deleteCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));

        cardRepository.delete(card);
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateExpiryDate() {
        LocalDate expiry = LocalDate.now().plusYears(4);
        return expiry.format(DateTimeFormatter.ofPattern("MM/yy"));
    }

    public String maskCardNumber(String encryptedCardNumber) {
        String decrypted = cipherService.decrypt(encryptedCardNumber);
        return "**** **** **** " + decrypted.substring(decrypted.length() - 4);
    }

    private String encrypt(String data) {
        return cipherService.encrypt(data);
    }

    public UserCardDto toUserDto(Card card) {
        UserCardDto dto = new UserCardDto();
        dto.setId(card.getId());
        dto.setNumber(maskCardNumber(card.getNumber()));
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }

    public AdminCardDto toAdminDto(Card card) {
        AdminCardDto dto = new AdminCardDto();
        dto.setId(card.getId());
        dto.setNumber(maskCardNumber(card.getNumber()));
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setOwnerId(card.getOwnerId());
        return dto;
    }

}
