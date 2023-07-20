package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error_handler.InvalidActorException;
import ru.practicum.shareit.error_handler.InvalidActorNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
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
            throw new InvalidActorNotFoundException("Владелец вещи не может ее забронировать");
        }
        if (!item.isAvailable()) {
            throw new InvalidActorException("Вещь " + itemId + " недоступна для бронирования");
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
            throw new InvalidActorException("Нельзя изменить статус бронирования после подтверждения");
        }
        if (userId != booking.getItem().getOwner().getId() && userId != booking.getBooker().getId()) {
            throw new InvalidActorException(
                    "У пользователя " + userId + " нет доступа к просмотру бронирования " + bookingId);
        }
        if (userId == booking.getBooker().getId()) {
            throw new InvalidActorNotFoundException(
                    "У арендатора " + userId + " нет доступа к изменению статуса бронирования " + bookingId);
        }
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(status);
        BookingResponseDto updated = mapper.toBookingResponseDto(bookingRepository.save(booking));
        log.info("Обновлен статус бронирования [{}] : {}", bookingId, updated);
        return updated;
    }

    @Override
    public BookingResponseDto findBooking(int bookingId, int userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (userId != booking.getItem().getOwner().getId() && userId != booking.getBooker().getId()) {
            throw new InvalidActorNotFoundException(
                    "У пользователя " + userId + " нет доступа к просмотру бронирования " + bookingId);
        }
        BookingResponseDto bookingResponseDto = mapper.toBookingResponseDto(booking);
        log.info("Вернули бронирование: {}", bookingResponseDto);
        return bookingResponseDto;
    }

    @Override
    public List<BookingResponseDto> findUserBookings(int userId, BookingState state) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        List<Booking> bookings = doFindUserBookings(userId, state);
        List<BookingResponseDto> bookingResponseDtos = mapper.toBookingResponseDto(bookings).stream()
                .sorted(Comparator.comparing(BookingResponseDto::getStart,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
        log.info("Вернули бронирования пользователя [{}] : {}", userId, bookingResponseDtos);
        return bookingResponseDtos;
    }

    private List<Booking> doFindUserBookings(int userId, BookingState state) {
        switch (state) {
            case ALL:
                return bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT:
                return bookingRepository.findAllByBookerIdAndCurrent(userId, LocalDateTime.now());
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case WAITING:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public List<BookingResponseDto> findOwnerItemsBookings(int userId, BookingState state) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        List<Booking> bookings = doFindOwnerItemsBookings(userId, state);
        List<BookingResponseDto> bookingResponseDtos = mapper.toBookingResponseDto(bookings);
        log.info("Вернули бронирования вещей пользователя [{}] : {}", userId, bookingResponseDtos);
        return bookingResponseDtos;
    }

    private List<Booking> doFindOwnerItemsBookings(int userId, BookingState state) {
        switch (state) {
            case ALL:
                return bookingRepository.findAllByOwnerItems(userId);
            case CURRENT:
                return bookingRepository.findAllByOwnerItemsAndCurrent(userId, LocalDateTime.now());
            case PAST:
                return bookingRepository.findAllByOwnerItemsAndPast(userId, LocalDateTime.now());
            case FUTURE:
                return bookingRepository.findAllByOwnerItemsAndFuture(userId, LocalDateTime.now());
            case WAITING:
                return bookingRepository.findAllByOwnerItemsAndStatus(userId, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findAllByOwnerItemsAndStatus(userId, BookingStatus.REJECTED);
            default:
                throw new IllegalArgumentException();
        }
    }
}
