package ru.practicum.shareit.request;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {

    List<ItemRequest> findAllByRequestorId(int requestorId);

    @Query(" select r from ItemRequest as r " +
            " join r.requestor as requestor " +
            " where requestor.id != :userId ")
    Page<ItemRequest> findAllWithoutUserRequests(int userId, Pageable pageable);
}
