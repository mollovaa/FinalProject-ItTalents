package ittalents.javaee1.models.dto;

import ittalents.javaee1.models.repository.UserRepository;

public interface UserDTOs {
	
	UserSessionDTO convertToUserSessionDTO();
	
	SearchableUserDTO convertToSearchableUserDTO();
	
	ViewProfileUserDTO convertToViewProfileUserDTO(UserRepository userRepository);
}
