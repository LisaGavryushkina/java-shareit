package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

public interface HasStartEndDate {
    LocalDateTime getStart();

    LocalDateTime getEnd();
}