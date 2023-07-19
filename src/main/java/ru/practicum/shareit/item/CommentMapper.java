package ru.practicum.shareit.item;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;

@Component
public class CommentMapper {
    public Comment toComment(CommentDto commentDto, Item item, User user, LocalDateTime now) {
        return new Comment(0,
                commentDto.getText(),
                item,
                user,
                now);
    }

    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getId(),
                comment.getText(),
                comment.getItem().getId(),
                comment.getAuthor().getName(),
                comment.getCreated());
    }
}
