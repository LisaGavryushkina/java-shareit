package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

public interface StartEndDateable {
    LocalDateTime getStart();

    LocalDateTime getEnd();
}