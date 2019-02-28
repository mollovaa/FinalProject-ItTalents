package ittalents.javaee1.models.dto;

import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ViewProfileUserDTO implements Searchable {
	private long userId;
	private int numberOfSubscribers;
	private String fullName;
	private List<Video> videos;
	private List<Playlist> playlists;
	
	@Override
	public SearchType getType() {
		return SearchType.USER;
	}
}
