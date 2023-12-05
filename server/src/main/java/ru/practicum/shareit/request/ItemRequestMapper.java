package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requestor, LocalDateTime now,
                                     List<Item> items) {
        return new ItemRequest(0,
                itemRequestDto.getDescription(),
                requestor,
                now,
                items
        );
    }

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        List<ItemDto> items = itemMapper.toItemDto(itemRequest.getItems());
        return new ItemRequestDto(itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequestor().getId(),
                itemRequest.getCreated(),
                items);
    }

    public List<ItemRequestDto> toItemRequestDto(List<ItemRequest> requests) {
        return requests.stream()
                .map(this::toItemRequestDto)
                .sorted(Comparator.comparing(ItemRequestDto::getCreated).reversed())
                .collect(Collectors.toList());
    }
}
