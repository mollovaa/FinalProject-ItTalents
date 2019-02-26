package ittalents.javaee1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class User implements Searchable {
	private long id;
	private int age;
	private String full_name;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String username;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String email;
	
	public User(int age, String full_name, String username, String password, String email) {
		this.age = age;
		this.full_name = full_name;
		this.username = username;
		this.password = password;
		this.email = email;
	}
	
	@Override
	public SearchType getType() {
		return SearchType.USER;
	}
}
