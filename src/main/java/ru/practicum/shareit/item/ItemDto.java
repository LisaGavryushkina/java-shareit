package ru.practicum.shareit.item;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class ItemDto {
    private final int id;
    @NotEmpty
    private final String name;
    @NotNull
    private final String description;
    @NotNull
    private final Boolean available;
    private final Integer request;
}
