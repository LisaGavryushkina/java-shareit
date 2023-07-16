package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {

    BookingDto addBooking(BookingDto bookingDto, int userId);

    BookingDto approveOrRejectBooking(int bookingId, int userId, boolean approved);

    BookingDto findBooking(int bookingId, int userId);

    List<BookingDto> findUserBookings(int userId, BookingState state);

    List<BookingDto> findOwnerItemsBookings(int userId, BookingState state);
}
