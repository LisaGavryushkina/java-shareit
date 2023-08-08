package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error_handler.ShareitInvalidArgumentException;
import ru.practicum.shareit.error_handler.ShareitNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper mapper;

    @Override
    public BookingResponseDto addBooking(BookingRequestDto bookingRequestDto, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        int itemId = bookingRequestDto.getItemId();
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        if (item.getOwner().getId() == userId) {
            throw new ShareitNotFoundException("Владелец вещи не может ее забронировать");
        }
        if (!item.isAvailable()) {
            throw new ShareitInvalidArgumentException("Вещь " + itemId + " недоступна для бронирования");
        }
        Booking booking = mapper.toBooking(bookingRequestDto, item, user, BookingStatus.WAITING);
        BookingResponseDto saved = mapper.toBookingResponseDto(bookingRepository.save(booking));
        log.info("Пользователь [{}] добавил новое бронирование: {}", userId, saved);
        return saved;
    }

    @Override
    public BookingResponseDto approveOrRejectBooking(int bookingId, int userId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ShareitInvalidArgumentException("Нельзя изменить статус бронирования после подтверждения");
        }
        if (userId == booking.getBooker().getId()) {
            throw new ShareitNotFoundException("Пользователь " + userId + " не может изменить статус бронирования");
        }
        if (userId != booking.getItem().getOwner().getId()) {
            throw new ShareitInvalidArgumentException(
                    "У пользователя " + userId + " нет доступа к изменению статуса бронирования " + bookingId);
        }
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(status);
        BookingResponseDto updated = mapper.toBookingResponseDto(bookingRepository.save(booking));
        log.info("Обновлен статус бронирования [{}] : {}", bookingId, updated);
        return updated;
    }

    @Override
    public BookingResponseDto findBooking(int bookingId, int userId) {
        Booking booking = bookingRepository.findByBookingIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        BookingResponseDto bookingResponseDto = mapper.toBookingResponseDto(booking);
        log.info("Вернули бронирование: {}", bookingResponseDto);
        return bookingResponseDto;
    }

    @Override
    public List<BookingResponseDto> findUserBookings(int userId, BookingState state, int from, int size) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Page<Booking> bookings = doFindUserBookings(userId, state, from, size);
        List<BookingResponseDto> bookingResponseDtos = bookings.map(mapper::toBookingResponseDto).getContent();
        log.info("Вернули бронирования пользователя [{}] : {}", userId, bookingResponseDtos);
        return bookingResponseDtos;
    }

    private Page<Booking> doFindUserBookings(int userId, BookingState state, int from, int size) {
        switch (state) {
            case ALL:
                return bookingRepository.findAllByBookerIdOrderByStartDesc(userId, new OffsetPageRequest(from, size,
                        Sort.by("start").descending()));
            case CURRENT:
                return bookingRepository.findAllByBookerIdAndCurrent(userId, LocalDateTime.now(),
                        new OffsetPageRequest(from, size));
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(),
                        new OffsetPageRequest(from, size));
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(),
                        new OffsetPageRequest(from, size));
            case WAITING:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING,
                        new OffsetPageRequest(from,
                        size));
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED,
                        new OffsetPageRequest(from,
                        size));
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public List<BookingResponseDto> findOwnerItemsBookings(int userId, BookingState state, int from, int size) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Page<Booking> bookings = doFindOwnerItemsBookings(userId, state, from, size);
        List<BookingResponseDto> bookingResponseDtos = bookings.map(mapper::toBookingResponseDto).getContent();
        log.info("Вернули бронирования вещей пользователя [{}] : {}", userId, bookingResponseDtos);
        return bookingResponseDtos;
    }

    private Page<Booking> doFindOwnerItemsBookings(int userId, BookingState state, int from, int size) {
        switch (state) {
            case ALL:
                return bookingRepository.findAllByOwnerItems(userId, new OffsetPageRequest(from, size));
            case CURRENT:
                return bookingRepository.findAllByOwnerItemsAndCurrent(userId, LocalDateTime.now(),
                        new OffsetPageRequest(from, size));
            case PAST:
                return bookingRepository.findAllByOwnerItemsAndPast(userId, LocalDateTime.now(),
                        new OffsetPageRequest(from, size));
            case FUTURE:
                return bookingRepository.findAllByOwnerItemsAndFuture(userId, LocalDateTime.now(),
                        new OffsetPageRequest(from, size));
            case WAITING:
                return bookingRepository.findAllByOwnerItemsAndStatus(userId, BookingStatus.WAITING,
                        new OffsetPageRequest(from, size));
            case REJECTED:
                return bookingRepository.findAllByOwnerItemsAndStatus(userId, BookingStatus.REJECTED,
                        new OffsetPageRequest(from, size));
            default:
                throw new IllegalArgumentException();
        }
    }
}
