package ru.practicum.shareit.booking;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Validated
public class BookingControllerGateway {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> postBooking(@RequestHeader("X-Sharer-User-Id") int userId,
                                              @Valid @RequestBody BookingRequestDto bookingDto) {
        return bookingClient.postBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveOrRejectBooking(@RequestHeader("X-Sharer-User-Id") int userId,
                                                         @PathVariable int bookingId,
                                                         @RequestParam boolean approved) {
        return bookingClient.approveOrRejectBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") int userId,
                                                 @PathVariable int bookingId) {
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                  @RequestParam(defaultValue = "ALL") BookingState state,
                                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(defaultValue = "15") @Min(1) int size) {
        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerItemsBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                        @RequestParam(defaultValue = "ALL") BookingState state,
                                                        @RequestParam(defaultValue = "0") @Min(0) int from,
                                                        @RequestParam(defaultValue = "15") @Min(1) int size) {
        return bookingClient.getOwnerItemsBookings(userId, state, from, size);
    }
}
