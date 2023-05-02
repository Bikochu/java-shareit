package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItems();

    ItemDto addItem(Long userId, ItemDto itemDto);

    List<ItemDto> getItemsByUser(Long userId);

    ItemDto findItemById(Long userId, Long itemId);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> searchItems(Long userId, String text);

    void removeItem(Long userId, Long itemId);
}
