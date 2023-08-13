package ru.practicum.shareit.item;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingForItemDto;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.error_handler.ShareitInvalidArgumentException;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestNotFoundException;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.ofNullable;
import static ru.practicum.shareit.item.ItemRepository.ItemWithBooking;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ItemRequestRepository itemRequestRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto addItem(ItemDto itemDto, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        ItemRequest request = null;
        Integer requestId = itemDto.getRequestId();
        if (requestId != null) {
            request = itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new ItemRequestNotFoundException(requestId));
        }
        Item item = itemRepository.save(itemMapper.toItem(itemDto, user, request));
        ItemDto added = itemMapper.toItemDto(item);
        log.info("Пользователь [{}] добавил вещь {}", userId, added);
        return added;
    }

    @Override
    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        if (item.getOwner().getId() != userId) {
            throw new UserNotFoundException(userId);
        }
        Item itemUpdated = itemRepository.save(itemMapper.toItemWithUpdate(itemDto, item));
        ItemDto itemDtoUpdated = itemMapper.toItemDto(itemUpdated);
        log.info("Пользователь [{}] обновил информацию о вещи {}", userId, itemDtoUpdated);
        return itemDtoUpdated;
    }

    @Override
    public ItemWithBookingAndCommentsDto getItemById(int itemId, int userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        ItemWithBooking itemWithBooking = itemRepository.findItemWithBookingsByItemId(itemId,
                LocalDateTime.now());
        BookingForItemDto last = null;
        BookingForItemDto next = null;
        if (item.getOwner().getId() == userId) {
            Integer lastId = itemWithBooking.getLastBooking();
            Integer nextId = itemWithBooking.getNextBooking();
            if (lastId != null) {
                last = bookingMapper.toBookingForItemDto(bookingRepository.findById(lastId)
                        .orElseThrow(() -> new BookingNotFoundException(lastId)));
            }
            if (nextId != null) {
                next = bookingMapper.toBookingForItemDto(bookingRepository.findById(nextId)
                        .orElseThrow(() -> new BookingNotFoundException(nextId)));
            }
        }
        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
        ItemWithBookingAndCommentsDto itemWithBookingAndCommentsDto =
                itemMapper.toItemWithBookingAndCommentsDto(itemWithBooking, last, next, comments);
        log.info("Вернули вещь: {}", itemWithBookingAndCommentsDto);
        return itemWithBookingAndCommentsDto;
    }

    @Override
    public List<ItemWithBookingAndCommentsDto> findOwnerItems(int userId, int from, int size) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Page<ItemWithBooking> items = itemRepository.findItemsWithBookingsByOwnerId(userId, LocalDateTime.now(),
                new OffsetPageRequest(from, size));
        Set<Integer> bookingIds = items.stream()
                .flatMap(it -> concat(ofNullable(it.getLastBooking()), ofNullable(it.getNextBooking())))
                .collect(Collectors.toSet());
        List<BookingForItemDto> bookings = bookingRepository.findAllById(bookingIds).stream()
                .map(bookingMapper::toBookingForItemDto)
                .collect(Collectors.toList());
        List<Integer> itemIds = items.stream()
                .map(ItemWithBooking::getId)
                .collect(Collectors.toList());
        List<CommentDto> comments = commentRepository.findAllByItemIdIn(itemIds).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
        List<ItemWithBookingAndCommentsDto> itemsWithDate =
                itemMapper.toItemWithBookingAndCommentsDto(items.getContent(), bookings, comments).stream()
                        .sorted(Comparator.comparing(item -> Optional.ofNullable(item.getLastBooking())
                                .map(BookingForItemDto::getStart)
                                .orElse(null), Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());
        log.info("Вернули все вещи пользователя [{}] : {}", userId, itemsWithDate);
        return itemsWithDate;
    }

    @Override
    public List<ItemDto> findItems(String text, int from, int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        Page<Item> items = itemRepository.findAllByText(text, new OffsetPageRequest(from, size));
        List<ItemDto> itemsDto = itemMapper.toItemDto(items.getContent());
        log.info("Вернули все вещи по описанию [{}] : {}", text, itemsDto);
        return itemsDto;
    }

    @Override
    public CommentDto postComment(int itemId, int userId, CommentDto commentDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        Booking booking = bookingRepository.findByItemIdAndBookerId(itemId, userId)
                .orElseThrow(() -> new ShareitInvalidArgumentException("Добавить комментарий можно только после оформления и" +
                        " окончания бронирования"));
        if (booking.getEnd().isAfter(LocalDateTime.now())) {
            throw new ShareitInvalidArgumentException("Добавить комментарий можно только после окончания периода " +
                    "бронирования");
        }
        Comment comment = commentMapper.toComment(commentDto, item, user, LocalDateTime.now());
        CommentDto saved = commentMapper.toCommentDto(commentRepository.save(comment));
        log.info("Добавили комментарий: {}", saved);
        return saved;
    }
}
