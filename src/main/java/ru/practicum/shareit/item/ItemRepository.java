package ru.practicum.shareit.item;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findAllByOwnerId(int ownerId);

    @Query(" select i from Item i " +
            " where ( upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%')) ) " +
            " and i.available = true ")
    List<Item> findAllByText(String text);

    @Query(nativeQuery = true, value = "" +
            " select i.id, " +
            "        i.name, " +
            "        i.description, " +
            "        i.is_available as available, " +
            "        i.owner_id as ownerId, " +
            "        i.request_id as requestId, " +
            " ( select b.id from bookings as b " +
            " where b.item_id = i.id " +
            " and b.start_date < :now " +
            " order by b.start_date desc " +
            " limit 1 ) as lastBooking, " +
            " ( select b.id from bookings as b " +
            " where b.item_id = i.id " +
            " and b.start_date > :now " +
            " order by b.start_date asc " +
            " limit 1 ) as nextBooking " +
            " from items as i " +
            " where i.owner_id = :ownerId ")
    List<ItemWithBooking> findItemsWithBookingsByOwnerId(int ownerId, LocalDateTime now);

    @Query(nativeQuery = true, value = "" +
            " select i.id, " +
            "        i.name, " +
            "        i.description, " +
            "        i.is_available as available, " +
            "        i.owner_id as ownerId, " +
            "        i.request_id as requestId, " +
            " ( select b.id from bookings as b " +
            " where b.item_id = i.id " +
            " and b.start_date < :now " +
            " and b.status != 'REJECTED' " +
            " order by b.start_date desc " +
            " limit 1 ) as lastBooking, " +
            " ( select b.id from bookings as b " +
            " where b.item_id = i.id " +
            " and b.start_date > :now " +
            " and b.status != 'REJECTED' " +
            " order by b.start_date asc " +
            " limit 1 ) as nextBooking " +
            " from items as i " +
            " where i.id = :itemId ")
    ItemWithBooking findItemWithBookingsByItemId(int itemId, LocalDateTime now);

    interface ItemWithBooking {

        int getId();

        String getName();

        String getDescription();

        boolean isAvailable();

        Integer getRequest();

        @Nullable
        Integer getLastBooking();

        @Nullable
        Integer getNextBooking();
    }
}
