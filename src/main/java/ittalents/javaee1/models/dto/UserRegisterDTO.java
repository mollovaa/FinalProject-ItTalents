package ittalents.javaee1.models.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserRegisterDTO extends UserLoginDTO {
	
	private String fullname;
	private String email;
	private int age;
	
	public UserRegisterDTO(String username, String password, String fullname, String email, int age) {
		super(username, password);
		this.fullname = fullname;
		this.email = email;
		this.age = age;
	}
}
