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
    public BookingDto addBooking(@Valid @RequestBody BookingDto bookingDto,
                                 @RequestHeader("X-Sharer-User-Id") int userId) {
        return bookingService.addBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveOrRejectBooking(@PathVariable int bookingId, @RequestHeader("X-Sharer-User-Id") int userId,
                                             @RequestParam boolean approved) {
        return bookingService.approveOrRejectBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findBooking(@PathVariable int bookingId, @RequestHeader("X-Sharer-User-Id") int userId) {
        return bookingService.findBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> findUserBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                             @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.findUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> findOwnerItemsBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                   @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.findOwnerItemsBookings(userId, state);
    }

}
