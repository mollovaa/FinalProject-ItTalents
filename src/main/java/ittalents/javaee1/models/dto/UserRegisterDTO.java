package ittalents.javaee1.models.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserRegisterDTO extends UserLoginDTO {
	
	private String fullName;
	private String email;
	private int age;
	
	public UserRegisterDTO(String username, String password, String fullName, String email, int age) {
		super(username, password);
		this.fullName = fullName;
		this.email = email;
		this.age = age;
	}
}
