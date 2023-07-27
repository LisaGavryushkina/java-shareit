package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import javax.validation.constraints.FutureOrPresent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@StartBeforeEndDateValid
public class BookingRequestDto implements StartEndDateable {
    private final int id;
    @FutureOrPresent
    private final LocalDateTime start;
    @FutureOrPresent
    private final LocalDateTime end;
    private final int itemId;
    private final int bookerId;
    private final BookingStatus status;

}