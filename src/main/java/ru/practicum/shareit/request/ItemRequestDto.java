package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.item.ItemDto;

/**
 * TODO Sprint add-item-requests.
 */
@ToString
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class ItemRequestDto {
    private int id;
    @NotBlank
    private String description;
    private int requestorId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;
    private List<ItemDto> items;
}
