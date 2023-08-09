package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query(" select b from Booking as b " +
            " left join b.booker as booker " +
            " left join b.item as item " +
            " left join item.owner as owner " +
            " where b.id = :bookingId " +
            " and (booker.id = :userId or owner.id = :userId) ")
    Optional<Booking> findByBookingIdAndUserId(int bookingId, int userId);

    Page<Booking> findByBookerIdAndStatus(Integer bookerId, BookingStatus status, Pageable pageable);

    @Query(" select b from Booking as b " +
            " join b.booker as booker " +
            " where booker.id = :bookerId " +
            " and :now between b.start and b.end ")
    Page<Booking> findAllByBookerIdAndCurrentOrderByStartDesc(int bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdOrderByStartDesc(int bookerId, Pageable pageable);

    Page<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(int bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(int bookerId, LocalDateTime now, Pageable pageable);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            " where owner.id = ?1 ) ")
    Page<Booking> findAllByOwnerItemsOrderByStartDesc(int ownerId, Pageable pageable);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            " where owner.id = ?1 ) " +
            " and ?2 between b.start and b.end ")
    Page<Booking> findAllByOwnerItemsAndCurrentOrderByStartDesc(int ownerId, LocalDateTime now, Pageable pageable);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and b.end < ?2 ")
    Page<Booking> findAllByOwnerItemsAndPastOrderByStartDesc(int ownerId, LocalDateTime now, Pageable pageable);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and b.start > ?2 ")
    Page<Booking> findAllByOwnerItemsAndFutureOrderByStartDesc(int ownerId, LocalDateTime now, Pageable pageable);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and b.status = ?2 ")
    Page<Booking> findAllByOwnerItemsAndStatusOrderByStartDesc(int ownerId, BookingStatus status, Pageable pageable);

    @Query(nativeQuery = true, value =
            " select b.* from bookings as b " +
                    " where b.item_id = :itemId " +
                    " and b.booker_id = :userId " +
                    " and b.status = 'APPROVED' " +
                    " limit 1 ")
    Optional<Booking> findByItemIdAndBookerId(int itemId, int userId);
}
