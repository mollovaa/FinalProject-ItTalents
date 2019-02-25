package ittalents.javaee1.models;


import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
public class User implements Searchable {
	private long id;
	private int age;
	private String full_name;
	private String username;
	private String password;
	private String email;
	
	public User(int age, String full_name, String username, String password, String email) {
		this.age = age;
		this.full_name = full_name;
		this.username = username;
		this.password = password;
		this.email = email;
	}
	
	public User(long id, String full_name) {
		this.id = id;
		this.full_name = full_name;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public int getAge() {
		return age;
	}
	
	public String getFull_name() {
		return full_name;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getEmail() {
		return email;
	}
	
	@Override
	public SearchType getType() {
		return SearchType.USER;
	}
}
