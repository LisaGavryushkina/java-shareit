package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
public class BookingDto {
    private final int id;
    @NotNull
    @FutureOrPresent
    private final LocalDateTime start;
    @NotNull
    @FutureOrPresent
    private final LocalDateTime end;
    @NotNull
    private final int itemId;
    private final int bookerId;
    private final BookingStatus status;

}
