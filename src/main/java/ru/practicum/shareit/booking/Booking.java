package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

/**
 * TODO Sprint add-bookings.
 */
@Entity
@Table(name = "bookings")
@ToString
@Getter
@EqualsAndHashCode
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final int id;

    @Column(name = "start_date", nullable = false)
    private final LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    private final LocalDateTime end;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    private final Item item;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    @ToString.Exclude
    private final User booker;

    @Setter
    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "status", nullable = false)
    private BookingStatus status;

}
