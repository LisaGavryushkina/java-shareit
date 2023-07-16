package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error_handler.AccessException;
import ru.practicum.shareit.error_handler.InvalidActorException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper mapper;

    @Override
    public BookingDto addBooking(BookingDto bookingDto, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        int itemId = bookingDto.getItemId();
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        if (!item.getAvailable()) {
            throw new AccessException("Вещь " + itemId + " недоступна для бронирования");
        }
        Booking booking = mapper.toBooking(bookingDto, 0, item, user, BookingStatus.WAITING);
        BookingDto saved = mapper.toBookingDto(bookingRepository.save(booking));
        log.info("Пользователь [{}] добавил новое бронирование: {}", userId, saved);
        return saved;
    }

    @Override
    public BookingDto approveOrRejectBooking(int bookingId, int userId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (userId != booking.getItem().getOwner().getId() && userId != booking.getBooker().getId()) {
            throw new AccessException(
                    "У пользователя " + userId + " нет доступа к просмотру бронирования " + bookingId);
        }
        if (userId == booking.getBooker().getId()) {
            throw new InvalidActorException(
                    "У арендатора " + userId + " нет доступа к изменению статуса бронирования " + bookingId);
        }
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(status);
        BookingDto updated = mapper.toBookingDto(bookingRepository.save(booking));
        log.info("Обновлен статус бронирования [{}] : {}", bookingId, updated);
        return updated;
    }

    @Override
    public BookingDto findBooking(int bookingId, int userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (userId != booking.getItem().getOwner().getId() && userId != booking.getBooker().getId()) {
            throw new InvalidActorException(
                    "У пользователя " + userId + " нет доступа к просмотру бронирования " + bookingId);
        }
        BookingDto bookingDto = mapper.toBookingDto(booking);
        log.info("Вернули бронирование: {}", bookingDto);
        return bookingDto;
    }

    @Override
    public List<BookingDto> findUserBookings(int userId, BookingState state) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        List<Booking> bookings = doFindUserBookings(userId, state);
        List<BookingDto> bookingsDto = mapper.toBookingDto(bookings);
        log.info("Вернули бронирования пользователя [{}] : {}", userId, bookingsDto);
        return bookingsDto;
    }

    private List<Booking> doFindUserBookings(int userId, BookingState state) {
        switch (state) {
            case ALL:
                return bookingRepository.findAllByBookerId(userId);
            case CURRENT:
                return bookingRepository.findAllByBookerIdAndCurrent(userId, LocalDateTime.now());
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndBefore(userId, LocalDateTime.now());
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartAfter(userId, LocalDateTime.now());
            case WAITING:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public List<BookingDto> findOwnerItemsBookings(int userId, BookingState state) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        List<Booking> bookings = doFindOwnerItemsBookings(userId,state);
        List<BookingDto> bookingsDto = mapper.toBookingDto(bookings);
        log.info("Вернули бронирования вещей пользователя [{}] : {}", userId, bookingsDto);
        return bookingsDto;
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
