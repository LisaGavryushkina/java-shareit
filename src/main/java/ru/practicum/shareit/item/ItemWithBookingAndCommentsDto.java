package ru.practicum.shareit.item;

import java.util.List;

import lombok.Data;
import ru.practicum.shareit.booking.BookingForItemDto;

@Data
public class ItemWithBookingAndCommentsDto {

    private final int id;
    private final String name;
    private final String description;
    private final Boolean available;
    private final Integer request;
    private final BookingForItemDto lastBooking;
    private final BookingForItemDto nextBooking;
    private final List<CommentDto> comments;
}
