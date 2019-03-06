package ittalents.javaee1.models.dto;

import ittalents.javaee1.models.repository.UserRepository;

public interface VideoDTOs {

    ViewVideoDTO convertToViewVideoDTO(UserRepository userRepository);

    SearchableVideoDTO convertToSearchableVideoDTO(UserRepository userRepository);

}
