package ru.practicum.shareit.item;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository  extends JpaRepository<Item, Integer> {

    List<Item> findAllByOwnerId(int ownerId);

    @Query(" select i from Item i " +
            " where upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%'))")
    List<Item> findAllByText(String text);
}
