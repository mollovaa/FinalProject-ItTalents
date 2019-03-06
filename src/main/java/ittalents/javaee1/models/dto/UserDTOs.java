package ittalents.javaee1.models.dto;

import ittalents.javaee1.models.repository.UserRepository;

public interface UserDTOs {
	
	UserSessionDTO convertToUserSessionDTO();
	
	SearchableUserDTO convertToSearchableDTO();
	
	ViewProfileUserDTO convertToViewProfileUserDTO(UserRepository userRepository);
}
