package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BookingForItemDto {
    private final int id;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final int itemId;
    private final int bookerId;
    private final BookingStatus status;
}
