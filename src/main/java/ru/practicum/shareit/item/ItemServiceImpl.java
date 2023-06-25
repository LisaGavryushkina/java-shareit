package ru.practicum.shareit.item;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto addItem(ItemDto itemDto, int userId) {
        userRepository.getUserById(userId);
        ItemDto added = itemRepository.addItem(itemDto, userId);
        log.info("Пользователь [{}] добавил вещь {}", userId, added);
        return added;
    }

    @Override
    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        ItemDto updated = itemRepository.updateItem(itemId, itemDto, userId);
        log.info("Пользователь [{}] обновил информацию о вещи {}", userId, updated);
        return updated;
    }

    @Override
    public ItemDto getItemById(int itemId) {
        ItemDto itemDto = itemRepository.getItemById(itemId);
        log.info("Вернули вещь: {}", itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllUserItems(int userId) {
        userRepository.getUserById(userId);
        List<ItemDto> items = itemRepository.getAllUserItems(userId);
        log.info("Вернули все вещи пользователя [{}] : {}", userId, items);
        return items;
    }

    @Override
    public List<ItemDto> findItems(String text) {
        List<ItemDto> items = itemRepository.findItems(text);
        log.info("Вернули все вещи по описанию [{}] : {}", text, items);
        return items;
    }
}
