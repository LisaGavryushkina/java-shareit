package ru.practicum.shareit.booking;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
@Validated
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
                                                     @RequestParam(defaultValue = "ALL") BookingState state,
                                                     @RequestParam(defaultValue = "0") @Min(0) int from,
                                                     @RequestParam(defaultValue = "15") @Min(1) int size) {
        return bookingService.findUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> findOwnerItemsBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                           @RequestParam(defaultValue = "ALL") BookingState state,
                                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                                           @RequestParam(defaultValue = "15") @Min(1) int size) {
        return bookingService.findOwnerItemsBookings(userId, state, from, size);
    }

}
