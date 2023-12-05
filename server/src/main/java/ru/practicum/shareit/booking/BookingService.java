package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {

    BookingResponseDto addBooking(BookingRequestDto bookingDto, int userId);

    BookingResponseDto approveOrRejectBooking(int bookingId, int userId, boolean approved);

    BookingResponseDto findBooking(int bookingId, int userId);

    List<BookingResponseDto> findUserBookings(int userId, BookingState state, int from, int size);

    List<BookingResponseDto> findOwnerItemsBookings(int userId, BookingState state, int from, int size);
}
