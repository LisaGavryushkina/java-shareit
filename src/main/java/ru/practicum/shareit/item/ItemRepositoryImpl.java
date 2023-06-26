package ru.practicum.shareit.item;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.UserNotFoundException;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Integer, Item> items;
    private int id = 0;
    private final ItemMapper mapper;

    private int createId() {
        return ++id;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, int userId) {
        Item item = mapper.toItem(createId(), itemDto, userId);
        items.put(item.getId(), item);
        return mapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new ItemNotFoundException(itemId);
        }
        if (item.getOwner() != userId) {
            throw new UserNotFoundException(userId);
        }
        Item updated = mapper.toItemWithUpdate(itemDto, item);
        items.put(updated.getId(), updated);
        return mapper.toItemDto(updated);
    }

    @Override
    public ItemDto getItemById(int itemId) {
        if (items.get(itemId) == null) {
            throw new ItemNotFoundException(itemId);
        }
        return mapper.toItemDto(items.get(itemId));
    }

    @Override
    public List<ItemDto> getAllUserItems(int userId) {
        return items.values().stream()
                .filter(item -> item.getOwner() == userId)
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(Item::getAvailable)
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }
}
