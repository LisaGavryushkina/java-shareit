package ru.practicum.shareit.item;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest());
    }

    public static Item toItem(int itemId, ItemDto itemDto, int ownerId) {
        return new Item(itemId,
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                ownerId,
                itemDto.getRequest());
    }

    public static Item toItemWithUpdate(ItemDto updated, Item item) {
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
