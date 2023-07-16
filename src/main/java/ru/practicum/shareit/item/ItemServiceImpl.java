package ru.practicum.shareit.item;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemMapper mapper;

    @Override
    public ItemDto addItem(ItemDto itemDto, int userId) {
        userService.getUserById(userId);
        Item item = itemRepository.save(mapper.toItem(0, itemDto, userId));
        ItemDto added = mapper.toItemDto(item);
        log.info("Пользователь [{}] добавил вещь {}", userId, added);
        return added;
    }

    @Override
    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new ItemNotFoundException(itemId);
        }
        Item item = itemOptional.get();
        if (item.getOwner().getId() != userId) {
            throw new UserNotFoundException(userId);
        }
        Item itemUpdated = itemRepository.save(mapper.toItemWithUpdate(itemDto, item));
        ItemDto itemDtoUpdated = mapper.toItemDto(itemUpdated);
        log.info("Пользователь [{}] обновил информацию о вещи {}", userId, itemDtoUpdated);
        return itemDtoUpdated;
    }

    @Override
    public ItemDto getItemById(int itemId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new ItemNotFoundException(itemId);
        }
        ItemDto itemDto = mapper.toItemDto(itemOptional.get());
        log.info("Вернули вещь: {}", itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllUserItems(int userId) {
        userService.getUserById(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<ItemDto> itemsDto = mapper.toItemDto(items);
        log.info("Вернули все вещи пользователя [{}] : {}", userId, itemsDto);
        return itemsDto;
    }

    @Override
    public List<ItemDto> findItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.findAllByText(text);
        List<ItemDto> itemsDto = mapper.toItemDto(items);
        log.info("Вернули все вещи по описанию [{}] : {}", text, itemsDto);
        return itemsDto;
    }
}
