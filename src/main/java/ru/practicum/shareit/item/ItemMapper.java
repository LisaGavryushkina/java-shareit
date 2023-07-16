package ru.practicum.shareit.item;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

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
                item.getAvailable(),
                request
        );
    }

    public Item toItem(int itemId, ItemDto itemDto, int ownerId) {
        return new Item(itemId,
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable());
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
}
