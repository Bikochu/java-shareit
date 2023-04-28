package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<Item> getItemsByUser(@RequestHeader(value = "X-Sharer-User-Id", required = true) Long userId) {
        return itemService.getItemsByUser(userId);
    }

    @GetMapping("/{itemId}")
    public Item findItemById(@RequestHeader(value = "X-Sharer-User-Id", required = true) Long userId,
                             @PathVariable("itemId") Long itemId) {
        return itemService.findItemById(userId, itemId);
    }

    @GetMapping("/search")
    public List<Item> searchItems(@RequestHeader(value = "X-Sharer-User-Id", required = true) Long userId,
                                  @RequestParam String text) {
        return itemService.searchItems(userId, text);
    }

    @PostMapping
    public Item addItem(@Valid @RequestHeader(value = "X-Sharer-User-Id", required = true) Long userId,
                        @Valid @RequestBody Item item) {
        return itemService.addItem(userId, item);
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@RequestHeader(value = "X-Sharer-User-Id", required = true) Long userId,
                           @PathVariable("itemId") Long itemId,
                           @RequestBody Item item) {
        return itemService.updateItem(userId, itemId, item);
    }
}
