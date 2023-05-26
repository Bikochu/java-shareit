package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundItemException;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDate;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {

    UserService userService;

    BookingRepository bookingRepository;

    ItemRepository itemRepository;

    CommentRepository commentRepository;

    RequestService requestService;

    RequestRepository requestRepository;

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
        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            ItemRequestDto requestDto = requestService.findItemRequestById(userId, itemDto.getRequestId());
            itemDto.setRequestId(requestDto.getId());
            request = requestRepository.findById(itemDto.getRequestId()).orElseThrow(() -> new RequestNotFoundException("Request not found."));
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setRequest(request);
        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDtoWithDate> getItemsByUser(Long userId, Integer from, Integer size) {
        User owner = UserMapper.toUser(userService.findUserById(userId));

        if (from != null && size != null) {
            if (from < 0 || size <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong request.");
            }
            int pageNumber = (int) Math.ceil((double) from / size);
            Pageable pageable = PageRequest.of(pageNumber, size);

            return itemRepository.findByOwner(owner, pageable).stream()
                    .map(ItemMapper::toItemDtoWithDate)
                    .peek(itemDto -> {
                        List<Booking> bookings = bookingRepository.findBookingByItemIdOrderByStartAsc(itemDto.getId());
                        LocalDateTime now = LocalDateTime.now();
                        BookingRequestDto lastBooking = null;
                        BookingRequestDto nextBooking = null;

                        for (Booking booking : bookings) {
                            if (booking.getEnd().isBefore(now)) {
                                lastBooking = BookingMapper.toBookingRequestDto(booking);
                            } else if (booking.getStart().isAfter(now)) {
                                nextBooking = BookingMapper.toBookingRequestDto(booking);
                                break;
                            }
                        }

                        itemDto.setLastBooking(lastBooking);
                        itemDto.setNextBooking(nextBooking);
                    })
                    .collect(Collectors.toList());
        }

        return itemRepository.findByOwner(owner).stream()
                .map(ItemMapper::toItemDtoWithDate)
                .peek(itemDto -> {
                    List<Booking> bookings = bookingRepository.findBookingByItemIdOrderByStartAsc(itemDto.getId());
                    LocalDateTime now = LocalDateTime.now();
                    BookingRequestDto lastBooking = null;
                    BookingRequestDto nextBooking = null;

                    for (Booking booking : bookings) {
                        if (booking.getEnd().isBefore(now)) {
                            lastBooking = BookingMapper.toBookingRequestDto(booking);
                        } else if (booking.getStart().isAfter(now)) {
                            nextBooking = BookingMapper.toBookingRequestDto(booking);
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

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto lastBooking = bookingRepository.findTopByItemOwnerIdAndStatusAndStartBeforeOrderByEndDesc(userId, Status.APPROVED, now)
                .map(BookingMapper::toBookingRequestDto)
                .orElse(null);

        BookingRequestDto nextBooking = bookingRepository.findTopByItemOwnerIdAndStatusAndStartAfterOrderByStartAsc(userId, Status.APPROVED, now)
                .map(BookingMapper::toBookingRequestDto)
                .orElse(null);

        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        ItemDtoWithDate itemDtoWithDate = ItemMapper.toItemDtoWithDate(item);
        itemDtoWithDate.setLastBooking(lastBooking);
        itemDtoWithDate.setNextBooking(nextBooking);
        itemDtoWithDate.setComments(comments);

        return itemDtoWithDate;
    }

    @Override
    public List<ItemDto> searchItems(Long userId, String text, Integer from, Integer size) {
        userService.findUserById(userId);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        if (from != null && size != null) {
            if (from < 0 || size <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong request.");
            }
            int pageNumber = (int) Math.ceil((double) from / size);
            Pageable pageable = PageRequest.of(pageNumber, size);

            return itemRepository.searchItems(text, pageable)
                    .stream()
                    .filter(Item::isAvailable)
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You already commented this item.");
        }
        List<Booking> bookings = bookingRepository.findBookingByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId, Status.APPROVED, LocalDateTime.now());

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
