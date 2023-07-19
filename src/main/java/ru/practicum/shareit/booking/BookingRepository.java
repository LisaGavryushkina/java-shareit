package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByBookerIdAndStatus(Integer bookerId, BookingStatus status);

    @Query(" select b from Booking as b " +
            " join b.booker as booker " +
            " where booker.id = ?1 " +
            " and ?2 between b.start and b.end " +
            " order by b.start desc ")
    List<Booking> findAllByBookerIdAndCurrent(Integer bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdOrderByStartDesc(int bookerId);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(int bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(int bookerId, LocalDateTime now);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            " where owner.id = ?1 ) " +
            " order by b.start desc ")
    List<Booking> findAllByOwnerItems(int ownerId);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            " where owner.id = ?1 ) " +
            " and ?2 between b.start and b.end " +
            " order by b.start desc ")
    List<Booking> findAllByOwnerItemsAndCurrent(int ownerId, LocalDateTime now);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and b.end < ?2 " +
            " order by b.start desc ")
    List<Booking> findAllByOwnerItemsAndPast(int ownerId, LocalDateTime now);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and b.start > ?2 " +
            " order by b.start desc ")
    List<Booking> findAllByOwnerItemsAndFuture(int ownerId, LocalDateTime now);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and b.status = ?2 " +
            " order by b.start desc ")
    List<Booking> findAllByOwnerItemsAndStatus(int ownerId, BookingStatus status);

    @Query(nativeQuery = true, value =
            " select b.* from bookings as b " +
                    " where b.item_id = :itemId " +
                    " and b.booker_id = :userId " +
                    " and b.status = 'APPROVED' " +
                    " limit 1 ")
    Optional<Booking> findByItemIdAndBookerId(int itemId, int userId);
}
