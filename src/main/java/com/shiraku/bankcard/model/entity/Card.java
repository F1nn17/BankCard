package com.shiraku.bankcard.model.entity;

import com.shiraku.bankcard.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Класс Card
 * - представляет собой сущность для хранения данных банковской карты
 * <p>
 * Включает следующие поля:
 * <p>
 * {@code id} - Уникальный идентификатор карты
 * <p>
 * {@code number} - Уникальный номер карты
 * <p>
 * {@code ownerId} - Владелец карты
 * <p>
 * {@code expiryDate} - Срок действия карты
 * <p>
 * {@code status} - Статус карты
 * <p>
 * {@code balance} - Баланс карты
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Tag(name = "Банковская карта", description = "Класс сущности банковской карты")
public class Card {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    @Schema(description = "Уникальный идентификатор карты")
    private UUID id;

    @Column(name = "number", updatable = false, nullable = false)
    @Schema(description = "Уникальный номер карты")
    private String number;

    @Column(name = "owner_id", updatable = false, nullable = false, columnDefinition = "UUID")
    @Schema(description = "Владелец карты")
    private UUID ownerId;

    @Column(name = "expiry_date", nullable = false)
    @Schema(description = "Срок действия карты")
    private String expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "Статус карты")
    private Status status;

    @Column(name = "balance", nullable = false)
    @Schema(description = "Баланс карты")
    private BigDecimal balance;

    public Card(UUID uuid, String s, Status status) {
        this.id = uuid;
        this.number = s;
        this.status = status;
    }
}
