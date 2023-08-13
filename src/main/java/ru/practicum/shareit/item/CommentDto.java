package ru.practicum.shareit.item;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@ToString
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class CommentDto {

    private int id;
    @NotBlank
    private String text;
    private int itemId;
    private String authorName;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime created;
}
