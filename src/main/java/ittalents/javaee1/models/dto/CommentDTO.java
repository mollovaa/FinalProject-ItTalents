package ittalents.javaee1.models.dto;

import ittalents.javaee1.models.repository.UserRepository;

public interface CommentDTO {

    ViewCommentDTO convertToViewCommentDTO(UserRepository userRepository);
}