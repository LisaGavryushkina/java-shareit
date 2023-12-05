package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class BookingForItemDto {
    private final int id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDateTime start;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDateTime end;
    private final int itemId;
    private final int bookerId;
    private final BookingStatus status;
}
