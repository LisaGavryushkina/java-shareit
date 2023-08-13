package ru.practicum.shareit.item;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * TODO Sprint add-controllers.
 */
@ToString
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class ItemDto {
    private int id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;
    private Integer requestId;
}
