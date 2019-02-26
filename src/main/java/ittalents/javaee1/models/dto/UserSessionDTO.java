package ittalents.javaee1.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class UserSessionDTO {
	private long id;
	private int age;
	private String username;
	private String fullname;
	private String email;
	
}
