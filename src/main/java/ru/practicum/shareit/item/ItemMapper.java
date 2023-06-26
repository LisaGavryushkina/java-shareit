package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemDto toItemDto(Item item) {
        return new ItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest());
    }

    public Item toItem(int itemId, ItemDto itemDto, int ownerId) {
        return new Item(itemId,
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                ownerId,
                itemDto.getRequest());
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
}
