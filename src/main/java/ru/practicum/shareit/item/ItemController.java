package ru.practicum.shareit.item;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO Sprint add-controllers.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") int userId) {
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable int itemId, @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") int userId) {
        return itemService.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingAndCommentsDto getItemById(@PathVariable int itemId,
                                                     @RequestHeader("X-Sharer-User-Id") int userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemWithBookingAndCommentsDto> findOwnerItems(@RequestHeader("X-Sharer-User-Id") int userId,
                                                              @RequestParam(defaultValue = "0") @Min(0) int from,
                                                              @RequestParam(defaultValue = "15") @Min(1) int size) {
        return itemService.findOwnerItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> findItems(@RequestParam(defaultValue = "") String text,
                                   @RequestParam(defaultValue = "0") @Min(0) int from,
                                   @RequestParam(defaultValue = "15") @Min(1) int size) {
        return itemService.findItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto postComment(@PathVariable int itemId, @RequestHeader("X-Sharer-User-Id") int userId,
                                  @Valid @RequestBody CommentDto commentDto) {
        return itemService.postComment(itemId, userId, commentDto);
    }


}
