package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.item.ItemDto;

@ToString
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class ItemRequestDto {
    private int id;
    private String description;
    private int requestorId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime created;
    private List<ItemDto> items;
}
