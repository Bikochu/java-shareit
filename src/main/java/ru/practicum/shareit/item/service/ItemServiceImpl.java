package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundItemException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDate;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserService userService;

    private final BookingRepository bookingRepository;

    private final ItemRepository itemRepository;

    private final CommentRepository commentRepository;

    @Override
    public List<ItemDto> getAllItems() {
        return itemRepository.findAll()
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        User owner = UserMapper.toUser(userService.findUserById(userId));
        itemDto.setOwner(UserMapper.toUserDto(owner));
        Item item = itemRepository.save(ItemMapper.toItem(itemDto));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDtoWithDate> getItemsByUser(Long userId) {
        User owner = UserMapper.toUser(userService.findUserById(userId));
        List<Item> items = itemRepository.findByOwner(owner);

        return items.stream()
                .map(ItemMapper::toItemDtoWithDate)
                .peek(itemDto -> {
                    List<Booking> bookings = bookingRepository.findBookingByItemId(itemDto.getId());
                    bookings.sort(Comparator.comparing(Booking::getStart));
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    BookingDtoIn lastBooking = null;
                    BookingDtoIn nextBooking = null;

                    for (Booking booking : bookings) {
                        if (booking.getEnd().isBefore(currentDateTime)) {
                            lastBooking = BookingMapper.toBookingDtoIn(booking);
                        } else if (booking.getStart().isAfter(currentDateTime)) {
                            nextBooking = BookingMapper.toBookingDtoIn(booking);
                            break;
                        }
                    }

                    itemDto.setLastBooking(lastBooking);
                    itemDto.setNextBooking(nextBooking);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        User owner = UserMapper.toUser(userService.findUserById(userId));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundItemException("Item not found."));
        if (!item.getOwner().equals(owner)) {
            throw new NotFoundItemException("Item not found.");
        }
        String name = itemDto.getName();
        String description = itemDto.getDescription();
        Boolean available = itemDto.getAvailable();
        if (name != null && !name.isBlank()) {
            item.setName(name);
        }
        if (description != null && !description.isBlank()) {
            item.setDescription(description);
        }
        if (available != null) {
            item.setAvailable(available);
        }
        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDtoWithDate findItemById(Long userId, Long itemId) {
        userService.findUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundItemException("Item not found."));

        List<BookingDtoIn> bookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getItem().getOwner().getId().equals(userId))
                .sorted(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toBookingDtoIn)
                .collect(Collectors.toList());

        LocalDateTime currentDateTime = LocalDateTime.now();
        BookingDtoIn lastBooking = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(currentDateTime) && booking.getStatus().equals("APPROVED"))
                .max(Comparator.comparing(BookingDtoIn::getEnd))
                .orElse(null);

        BookingDtoIn nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(currentDateTime) && booking.getStatus().equals("APPROVED"))
                .findFirst()
                .orElse(null);

        List<CommentDto> comments = commentRepository.findAll().stream()
                .filter(comment -> comment.getItem().getId().equals(itemId))
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        ItemDtoWithDate itemDtoWithDate = ItemMapper.toItemDtoWithDate(item);
        itemDtoWithDate.setLastBooking(lastBooking);
        itemDtoWithDate.setNextBooking(nextBooking);
        itemDtoWithDate.setComments(comments);

        return itemDtoWithDate;
    }

    @Override
    public List<ItemDto> searchItems(Long userId, String text) {
        userService.findUserById(userId);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchItems(text)
                .stream()
                .filter(Item::isAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void removeItem(Long userId, Long itemId) {
        findItemById(userId, itemId);
        itemRepository.deleteById(itemId);
    }

    @Override
    public Item findItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new NotFoundItemException("Item not found."));
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        UserDto author = userService.findUserById(userId);
        ItemDto item = ItemMapper.toItemDto(findItem(itemId));
        Comment existingComment = commentRepository.findByAuthorIdAndItemId(userId, itemId);
        if (existingComment != null) {
            return CommentMapper.toCommentDto(existingComment);
        }
        List<Booking> bookings = bookingRepository.findBookingByItemId(itemId).stream()
                .filter(booking -> booking.getBooker().getId().equals(userId)
                        && booking.getStatus().equals(Status.APPROVED)
                        && booking.getEnd().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (bookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't comment.");
        }
        commentDto.setItemDto(item);
        commentDto.setAuthorName(author.getName());
        commentDto.setCreated(LocalDateTime.now());
        Comment comment = commentRepository.save(CommentMapper.toComment(commentDto, UserMapper.toUser(author)));
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getAllComments() {
        return commentRepository.findAll().stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
