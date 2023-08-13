package ru.practicum.shareit.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingForItemDto;
import ru.practicum.shareit.item.ItemRepository.ItemWithBooking;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Component
public class ItemMapper {

    public ItemDto toItemDto(Item item) {
        Integer request = null;
        if (item.getRequest() != null) {
            request = item.getRequest().getId();
        }
        return new ItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                request
        );
    }

    public Item toItem(ItemDto itemDto, User owner, ItemRequest request) {
        return new Item(0,
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                request);
    }

    public Item toItemWithUpdate(ItemDto updated, Item item) {
        if (updated.getName() != null) {
            item.setName(updated.getName());
        }
        if (updated.getDescription() != null) {
            item.setDescription(updated.getDescription());
        }
        if (updated.getAvailable() != null) {
            item.setAvailable(updated.getAvailable());
        }
        return item;
    }

    public List<ItemDto> toItemDto(List<Item> items) {
        return items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    private ItemDto toItemDto(ItemWithBooking itemWithBooking) {
        return new ItemDto(itemWithBooking.getId(),
                itemWithBooking.getName(),
                itemWithBooking.getDescription(),
                itemWithBooking.isAvailable(),
                itemWithBooking.getRequest());
    }


    public List<ItemWithBookingAndCommentsDto> toItemWithBookingAndCommentsDto(List<ItemWithBooking> items,
                                                                               List<BookingForItemDto> bookings,
                                                                               List<CommentDto> comments) {
        Map<Integer, BookingForItemDto> bookingsById = bookings.stream()
                .collect(Collectors.toMap(BookingForItemDto::getId, b -> b));
        Map<Integer, List<CommentDto>> commentsByItemId = comments.stream()
                .collect(Collectors.groupingBy(CommentDto::getItemId));
        List<ItemWithBookingAndCommentsDto> itemWithBookingsDto = new ArrayList<>();

        for (ItemWithBooking item : items) {
            BookingForItemDto last = Optional.ofNullable(item.getLastBooking()).map(bookingsById::get).orElse(null);
            BookingForItemDto next = Optional.ofNullable(item.getNextBooking()).map(bookingsById::get).orElse(null);
            ItemWithBookingAndCommentsDto itemWithBookingDto =
                    new ItemWithBookingAndCommentsDto(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.isAvailable(),
                            item.getRequest(),
                            last,
                            next,
                            commentsByItemId.getOrDefault(item.getId(), Collections.emptyList()));
            itemWithBookingsDto.add(itemWithBookingDto);
        }
        return itemWithBookingsDto;
    }

    public ItemWithBookingAndCommentsDto toItemWithBookingAndCommentsDto(ItemWithBooking itemWithBooking,
                                                                         BookingForItemDto last, BookingForItemDto next,
                                                                         List<CommentDto> comments) {
        return new ItemWithBookingAndCommentsDto(itemWithBooking.getId(),
                itemWithBooking.getName(),
                itemWithBooking.getDescription(),
                itemWithBooking.isAvailable(),
                itemWithBooking.getRequest(),
                last,
                next,
                comments);
    }

}
