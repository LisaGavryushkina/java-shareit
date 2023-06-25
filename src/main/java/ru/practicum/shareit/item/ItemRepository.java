package ru.practicum.shareit.item;

import java.util.List;

public interface ItemRepository {

    ItemDto addItem(ItemDto itemDto, int userId);

    ItemDto updateItem(int itemId, ItemDto itemDto, int userId);

    ItemDto getItemById(int itemId);

    List<ItemDto> getAllUserItems(int userId);

    List<ItemDto> findItems(String text);
}
