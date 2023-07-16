package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByBookerIdAndStatus(Integer bookerId, BookingStatus status);

    @Query(" select b from Booking as b " +
            " join b.booker as booker " +
            " where booker.id = ?1 " +
            " and ?2 between b.start and b.end ")
    List<Booking> findAllByBookerIdAndCurrent(Integer bookerId, LocalDateTime now);

    List<Booking> findAllByBookerId(int bookerId);

    List<Booking> findAllByBookerIdAndEndBefore(int bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStartAfter(int bookerId, LocalDateTime now);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 )")
    List<Booking> findAllByOwnerItems(int ownerId);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and ?2 between b.start and b.end ")
    List<Booking> findAllByOwnerItemsAndCurrent(int ownerId, LocalDateTime now);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and b.end before ?2 ")
    List<Booking> findAllByOwnerItemsAndPast(int ownerId, LocalDateTime now);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and b.start after ?2 ")
    List<Booking> findAllByOwnerItemsAndFuture(int ownerId, LocalDateTime now);

    @Query(" select b from Booking as b " +
            " join b.item as item " +
            " where item.id in " +
            "( select i.id from Item as i " +
            " join i.owner as owner " +
            "where owner.id = ?1 ) " +
            " and b.status = ?2 ")
    List<Booking> findAllByOwnerItemsAndStatus(int ownerId, BookingStatus status);
}
