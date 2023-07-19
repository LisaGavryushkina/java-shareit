package ru.practicum.shareit.booking;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public Booking toBooking(BookingRequestDto bookingRequestDto, Item item, User booker, BookingStatus status) {
        return new Booking(
                0,
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                item,
                booker,
                status);
    }

    public BookingResponseDto toBookingResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(itemMapper.toItemDto(booking.getItem()))
                .booker(userMapper.toUserDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }

    public List<BookingResponseDto> toBookingResponseDto(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    public BookingForItemDto toBookingForItemDto(Booking b) {
        return new BookingForItemDto(
                b.getId(),
                b.getStart(),
                b.getEnd(),
                b.getItem().getId(),
                b.getBooker().getId(),
                b.getStatus()
        );
    }
}
