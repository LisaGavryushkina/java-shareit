package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(ItemRequestDto requestDto, int requestorId, LocalDateTime now);

    List<ItemRequestDto> findUserRequests(int requestorId);

    List<ItemRequestDto> findAllRequests(int from, int size, int userId);

    ItemRequestDto findRequestById(int requestId, int userId);
}
