package ittalents.javaee1.models.dto;

import ittalents.javaee1.models.repository.UserRepository;

public interface VideoDTOs {

    ViewVideoDTO convertToViewVideoDTO(String uploaderName);

    SearchableVideoDTO convertToSearchableVideoDTO(UserRepository userRepository);

}
