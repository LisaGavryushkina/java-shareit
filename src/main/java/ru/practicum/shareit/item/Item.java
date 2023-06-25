package ru.practicum.shareit.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TODO Sprint add-controllers.
 */
@ToString
@Getter
@EqualsAndHashCode
public class Item {
    private final int id;
    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    private Boolean available;
    private final int owner;
    private final Integer request;

    public Item(int id, String name, String description, Boolean available, int owner, Integer request) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.owner = owner;
        this.request = request;
    }
}
