package ru.practicum.shareit.request;

import java.time.LocalDateTime;

import lombok.Data;
import ru.practicum.shareit.user.User;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequest {
    private final int id;
    private final String description;
    private final User requestor;
    private final LocalDateTime created;
}
