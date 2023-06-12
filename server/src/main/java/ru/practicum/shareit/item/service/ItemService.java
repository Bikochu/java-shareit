package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDate;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long userId, ItemDto itemDto);

    List<ItemDtoWithDate> getItemsByUser(Long userId, Integer from, Integer size);

    ItemDtoWithDate findItemById(Long userId, Long itemId);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> searchItems(Long userId, String text, Integer from, Integer size);

    Item findItem(Long itemId);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
