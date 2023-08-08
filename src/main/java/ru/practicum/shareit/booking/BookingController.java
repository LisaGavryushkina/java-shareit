package ru.practicum.shareit.booking;

import java.util.List;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto addBooking(@Valid @RequestBody BookingRequestDto bookingDto,
                                         @RequestHeader("X-Sharer-User-Id") int userId) {
        return bookingService.addBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveOrRejectBooking(@PathVariable int bookingId,
                                                     @RequestHeader("X-Sharer-User-Id") int userId,
                                                     @RequestParam boolean approved) {
        return bookingService.approveOrRejectBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findBooking(@PathVariable int bookingId, @RequestHeader("X-Sharer-User-Id") int userId) {
        return bookingService.findBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> findUserBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                     @RequestParam(defaultValue = "ALL") String state,
                                                     @RequestParam(defaultValue = "0") int from,
                                                     @RequestParam(defaultValue = "15") int size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Параметры from и size не могут быть отрицательными");
        }
        return bookingService.findUserBookings(userId, parseBookingState(state), from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> findOwnerItemsBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                           @RequestParam(defaultValue = "ALL") String state,
                                                           @RequestParam(defaultValue = "0") int from,
                                                           @RequestParam(defaultValue = "15") int size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Параметры from и size не могут быть отрицательными");
        }
        return bookingService.findOwnerItemsBookings(userId, parseBookingState(state), from, size);
    }

    private static BookingState parseBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state, e);
        }
    }
}
