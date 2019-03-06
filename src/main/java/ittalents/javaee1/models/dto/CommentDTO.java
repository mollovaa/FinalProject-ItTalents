package ittalents.javaee1.models.dto;

import ittalents.javaee1.repository.UserRepository;

public interface CommentDTO {

    ViewCommentDTO convertToCommentDTO(UserRepository userRepository);
}
