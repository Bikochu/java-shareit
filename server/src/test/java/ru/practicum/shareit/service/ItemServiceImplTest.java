package ru.practicum.shareit.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDate;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceImplTest {

    @Mock
    UserService userService;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    ItemRepository itemRepository;

    @Mock
    CommentRepository commentRepository;

    @Mock
    RequestService requestService;

    @Mock
    RequestRepository requestRepository;

    @InjectMocks
    ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemServiceImpl(userService, bookingRepository, itemRepository, commentRepository, requestService, requestRepository);
    }

    @Test
    void addItem_withRequests() {
        //Создаем сущности.
        Long userId = 1L;
        Long requestId = 2L;
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        User user = new User(userId, "Svetlana", "sveta@mail.com");
        User owner = new User(userId, "Svetlana", "sveta@mail.com");
        UserDto ownerDto = UserMapper.toUserDto(owner);
        ItemRequest itemRequest = new ItemRequest(2L, "Need fork for eating.", user, now);
        ItemRequestDto itemRequestDto = RequestMapper.toItemRequestDto(itemRequest);
        Item item = new Item(1L, "Fork", "Kitchen thing", true, user, itemRequest);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        when(userService.findUserById(userId)).thenReturn(ownerDto);

        when(requestService.findItemRequestById(userId, requestId)).thenReturn(itemRequestDto);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        //Тестируем.
        ItemDto result = itemService.addItem(userId, itemDto);

        //Делаем проверки.
        assertNotNull(result);
        assertEquals(ItemMapper.toItemDto(item), result);
    }

    @Test
    void addItem_WithoutRequests() {
        //Создаем сущности.
        Long userId = 1L;
        User user = new User(userId, "Svetlana", "sveta@mail.com");
        User owner = new User(userId, "Svetlana", "sveta@mail.com");
        UserDto ownerDto = UserMapper.toUserDto(owner);
        Item item = new Item(1L, "Fork", "Kitchen thing", true, user, null);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        when(userService.findUserById(anyLong())).thenReturn(ownerDto);

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        //Тестируем.
        ItemDto result = itemService.addItem(userId, itemDto);

        //Делаем проверки.
        assertNotNull(result);
        assertEquals(ItemMapper.toItemDto(item), result);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void getItemsByUser_WithPages_ErrorResponse() {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 0;

        User owner = new User();
        owner.setId(userId);
        UserDto ownerDto = UserMapper.toUserDto(owner);
        when(userService.findUserById(userId)).thenReturn(ownerDto);

        assertThrows(ResponseStatusException.class, () -> itemService.getItemsByUser(userId, from, size));
    }

    @Test
    void getItemsByUser_WithPages() {
        //Создаем сущности.
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        User owner = new User();
        owner.setId(userId);
        UserDto ownerDto = UserMapper.toUserDto(owner);
        when(userService.findUserById(userId)).thenReturn(ownerDto);

        List<Item> items = new ArrayList<>();
        Item item = new Item(1L, "Fork", "Kitchen thing", true, owner, null);
        items.add(item);
        List<ItemDtoWithDate> itemDtoWithDatesList = items.stream().map(ItemMapper::toItemDtoWithDate).collect(Collectors.toList());
        Page<Item> page = new PageImpl<>(items);
        when(itemRepository.findByOwner(owner, PageRequest.of(0, 10))).thenReturn(page);

        //Тестируем.
        List<ItemDtoWithDate> result = itemService.getItemsByUser(userId, from, size);

        //Делаем проверки.
        assertEquals(items.size(), result.size());
        assertEquals(itemDtoWithDatesList, result);
        verify(itemRepository, times(1)).findByOwner(owner, PageRequest.of(0, 10));
    }

    @Test
    void getItemsByUser_WithoutPages() {
        //Создаем сущности.
        Long userId = 1L;
        User owner = new User();
        UserDto ownerDto = new UserDto();
        when(userService.findUserById(userId)).thenReturn(ownerDto);
        List<Item> items = new ArrayList<>();
        items.add(new Item(1L, "Fork", "Kitchen thing", true, owner, null));
        when(itemRepository.findByOwner(owner)).thenReturn(items);

        //Тестируем.
        List<ItemDtoWithDate> result = itemService.getItemsByUser(userId, null, null);

        //Проверяем.
        assertEquals(items.size(), result.size());
        assertEquals(items.stream().map(ItemMapper::toItemDtoWithDate).collect(Collectors.toList()), result);
        verify(itemRepository, times(1)).findByOwner(owner);
    }

    @Test
    void updateItem() {
        //Создаем сущности.
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto itemDto = new ItemDto();
        UserDto ownerDto = new UserDto();
        User owner = new User();
        ItemRequest request = new ItemRequest();
        Item item = new Item(1L, "Fork", "Kitchen thing", true, owner, request);
        when(userService.findUserById(userId)).thenReturn(ownerDto);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(firstItem -> firstItem.getArgument(0));

        //Тестируем.
        ItemDto result = itemService.updateItem(userId, itemId, itemDto);

        //Проверяем.
        assertNotNull(result);
        assertEquals(ItemMapper.toItemDto(item), result);
        verify(userService, times(1)).findUserById(userId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void findItemById() {
        //Создаем сущности.
        Long userId = 1L;
        Long itemId = 1L;
        UserDto userDto = new UserDto(userId, "Svetlana", "sveta@mail.com");
        User user = new User(userId, "Svetlana", "sveta@mail.com");

        Item item = new Item(1L, "Fork", "Kitchen thing", true, user, null);
        when(userService.findUserById(userId)).thenReturn(userDto);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findTopByItemOwnerIdAndStatusAndStartBeforeOrderByEndDesc(anyLong(), any(Status.class), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(bookingRepository.findTopByItemOwnerIdAndStatusAndStartAfterOrderByStartAsc(anyLong(), any(Status.class), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(commentRepository.findAllByItemId(itemId)).thenReturn(Collections.emptyList());

        //Тестируем.
        ItemDtoWithDate result = itemService.findItemById(userId, itemId);

        //Проверяем.
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        verify(userService, times(1)).findUserById(userId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1)).findTopByItemOwnerIdAndStatusAndStartBeforeOrderByEndDesc(anyLong(), any(Status.class), any(LocalDateTime.class));
        verify(bookingRepository, times(1)).findTopByItemOwnerIdAndStatusAndStartAfterOrderByStartAsc(anyLong(), any(Status.class), any(LocalDateTime.class));
        verify(commentRepository, times(1)).findAllByItemId(itemId);
    }

    @Test
    void searchItems_WithPages_ErrorResponse() {
        Long userId = 1L;
        String text = "";
        Integer from = 0;
        Integer size = 10;
        User user = new User();
        user.setId(userId);
        UserDto userDto = UserMapper.toUserDto(user);
        when(userService.findUserById(userId)).thenReturn(userDto);

        List<ItemDto> result = itemService.searchItems(userId, text, from, size);

        assertEquals(new ArrayList<>(), result);
    }

    @Test
    void searchItems_WithPages_PageNull() {
        Long userId = 1L;
        String text = "search";
        Integer from = 0;
        Integer size = 0;
        User user = new User();
        user.setId(userId);
        UserDto userDto = UserMapper.toUserDto(user);
        when(userService.findUserById(userId)).thenReturn(userDto);

        assertThrows(ResponseStatusException.class, () -> itemService.searchItems(userId, text, from, size));
    }

    @Test
    void searchItems_WithPages() {
        // Создаем сущности.
        Long userId = 1L;
        String text = "search";
        Integer from = 0;
        Integer size = 10;
        User user = new User();
        user.setId(userId);
        UserDto userDto = UserMapper.toUserDto(user);
        when(userService.findUserById(userId)).thenReturn(userDto);

        List<Item> items = new ArrayList<>();
        items.add(new Item(1L, "Fork", "Kitchen thing", true, user, null));
        Page<Item> page = new PageImpl<>(items);
        when(itemRepository.searchItems(text, PageRequest.of(0, 10))).thenReturn(page);

        //Тестируем.
        List<ItemDto> result = itemService.searchItems(userId, text, from, size);

        //Проверяем.
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList()), result);
        verify(userService, times(1)).findUserById(userId);
        verify(itemRepository, times(1)).searchItems(text, PageRequest.of(0, 10));
    }

    @Test
    void searchItems_WithoutPages() {
        // Создаем сущность.
        Long userId = 1L;
        String text = "search";
        User user = new User();
        user.setId(userId);
        UserDto userDto = UserMapper.toUserDto(user);
        List<Item> items = new ArrayList<>();
        items.add(new Item(1L, "Fork", "Kitchen thing", true, user, null));

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(itemRepository.searchItems(text)).thenReturn(items);

        //Тестируем.
        List<ItemDto> result = itemService.searchItems(userId, text, null, null);

        //Проверяем.
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList()), result);
        verify(userService, times(1)).findUserById(userId);
        verify(itemRepository, times(1)).searchItems(text);
    }

    @Test
    void findItem() {
        //Создаем сущность.
        Long itemId = 1L;
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        Item item = new Item(1L, "Fork", "Kitchen thing", true, user, null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        //Тестируем.
        Item result = itemService.findItem(itemId);

        //Проверяем.
        assertEquals(itemId, result.getId());
        assertEquals(item, result);
        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void addComment() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        Long userId = 1L;
        Long itemId = 1L;
        User author = new User();
        author.setId(userId);
        UserDto authorDto = UserMapper.toUserDto(author);
        User user = new User();
        user.setId(userId);
        Item item = new Item(1L, "Fork", "Kitchen thing", true, user, null);
        Comment comment = new Comment(1L, "Nice fork", item, author, now);
        CommentDto commentDto = CommentMapper.toCommentDto(comment);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking());

        when(userService.findUserById(userId)).thenReturn(authorDto);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findByAuthorIdAndItemId(anyLong(), anyLong())).thenReturn(null);
        when(bookingRepository.findBookingByItemIdAndBookerIdAndStatusAndEndBefore(anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(commentRepository.save(any(Comment.class))).thenAnswer(firstComment -> firstComment.getArgument(0));

        //Тестируем.
        CommentDto result = itemService.addComment(userId, itemId, commentDto);

        //Проверяем.
        assertEquals(commentDto, result);
        verify(userService, times(1)).findUserById(userId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(commentRepository, times(1)).findByAuthorIdAndItemId(userId, itemId);
        verify(bookingRepository, times(1)).findBookingByItemIdAndBookerIdAndStatusAndEndBefore(anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
    }
}