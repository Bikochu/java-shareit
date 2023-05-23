package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDate;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                UserMapper.toUserDto(item.getOwner()),
                item.getRequest() != null ? item.getRequest().getId() : null,
                null
        );
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.isAvailable(),
                UserMapper.toUser(itemDto.getOwner()),
                null
        );
    }

    public static ItemDtoWithDate toItemDtoWithDate(Item item) {
        return new ItemDtoWithDate(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                UserMapper.toUserDto(item.getOwner()),
                item.getRequest() != null ? item.getRequest().getId() : null,
                null,
                null,
                null
        );
    }
}
