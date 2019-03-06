package ittalents.javaee1.models.dto;

import ittalents.javaee1.models.repository.UserRepository;

public interface PlaylistDTOs {

    SearchablePlaylistDTO convertToSearchablePlaylistDTO(UserRepository userRepository);

    ViewPlaylistDTO convertToViewPlaylistDTO(UserRepository userRepository);
}
