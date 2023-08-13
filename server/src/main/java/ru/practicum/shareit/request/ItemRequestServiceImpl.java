package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto addRequest(ItemRequestDto requestDto, int requestorId, LocalDateTime now) {
        User requestor = userRepository.findById(requestorId).orElseThrow(() -> new UserNotFoundException(requestorId));
        ItemRequest request = itemRequestMapper.toItemRequest(requestDto, requestor, now, new ArrayList<>());
        ItemRequestDto saved = itemRequestMapper.toItemRequestDto(itemRequestRepository.save(request));
        log.info("Сохранили новый запрос вещи: {}", saved);
        return saved;
    }

    @Override
    public List<ItemRequestDto> findUserRequests(int requestorId) {
        User requestor = userRepository.findById(requestorId).orElseThrow(() -> new UserNotFoundException(requestorId));
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorId(requestorId);
        List<ItemRequestDto> requestDtos = itemRequestMapper.toItemRequestDto(requests);
        log.info("Вернули все запросы пользователя [{}] : {}", requestorId, requestDtos);
        return requestDtos;
    }

    @Override
    public List<ItemRequestDto> findAllRequests(int from, int size, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Page<ItemRequest> requests = itemRequestRepository.findAllWithoutUserRequests(userId,
                new OffsetPageRequest(from, size,
                        Sort.by("created").descending()));
        List<ItemRequestDto> requestDtos = requests.map(itemRequestMapper::toItemRequestDto).getContent();
        log.info("Вернули запросы: {}", requestDtos);
        return requestDtos;
    }

    @Override
    public ItemRequestDto findRequestById(int requestId, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ItemRequestNotFoundException(requestId));
        ItemRequestDto requestDto = itemRequestMapper.toItemRequestDto(request);
        log.info("Вернули запрос: {}", requestDto);
        return requestDto;
    }
}
