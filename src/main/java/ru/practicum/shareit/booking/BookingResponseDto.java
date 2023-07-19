package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

/**
 * TODO Sprint add-bookings.
 */
@ToString
@Getter
@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponseDto {
    private final int id;
    @Setter
    private LocalDateTime start;
    @Setter
    private LocalDateTime end;
    private final ItemDto item;
    private final UserDto booker;
    private final BookingStatus status;

}
