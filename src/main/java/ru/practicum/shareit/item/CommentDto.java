package ru.practicum.shareit.item;

import java.time.LocalDateTime;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class CommentDto {

    private final int id;
    @NotEmpty
    private final String text;
    private final int itemId;
    private final String authorName;
    private final LocalDateTime created;
}
