package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import javax.validation.constraints.FutureOrPresent;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
@StartBeforeEndDateValid
public class BookingRequestDto implements HasStartEndDate {
    private int id;
    @FutureOrPresent
    private LocalDateTime start;
    @FutureOrPresent
    private LocalDateTime end;
    private int itemId;
    private int bookerId;
    private BookingStatus status;

}