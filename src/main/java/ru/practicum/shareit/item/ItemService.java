package ru.practicum.shareit.item;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemService {

    ItemDto addItem(ItemDto itemDto, int userId);

    ItemDto updateItem(int itemId, ItemDto itemDto, int userId);

    ItemWithBookingAndCommentsDto getItemById(int itemId, int userId);

    List<ItemWithBookingAndCommentsDto> findOwnerItems(int userId);

    List<ItemDto> findItems(String text);

    CommentDto postComment(int itemId, int userId, CommentDto commentDto, LocalDateTime now);
}
