
package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequesterIdOrderByCreatedAsc(Long userId);

    List<ItemRequest> findByRequesterIdNot(Long userId);

    Page<ItemRequest> findAll(Pageable pageable);

    Page<ItemRequest> findByRequesterIdNot(Long userId, Pageable pageable);
}

