package ittalents.javaee1.models.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ittalents.javaee1.models.dto.SearchableVideoDTO;
import ittalents.javaee1.models.dto.VideoDTOs;
import ittalents.javaee1.models.dto.ViewVideoDTO;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import ittalents.javaee1.repository.UserRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "videos")
public class Video implements Searchable, VideoDTOs {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long videoId;
	private String title;
	private String category;
	private String description;
	private String URL;
	private LocalDate uploadDate;
	private long duration;        //seconds
	private boolean isPrivate;
	private int numberOfLikes;
	private int numberOfDislikes;
	private long numberOfViews;
	private long uploaderId;
	
	
	@JsonIgnore
	@ManyToMany(mappedBy = "likedVideos")
	private List<User> usersLikedVideo = new ArrayList<>();
	
	@JsonIgnore
	@ManyToMany(mappedBy = "dislikedVideos")
	private List<User> usersDislikedVideo = new ArrayList<>();
	
	@JsonIgnore
	@ManyToMany(mappedBy = "videosInPlaylist")
	private List<Playlist> playlistContainingVideo = new ArrayList<>();
	
	@OneToMany(mappedBy = "videoId", orphanRemoval = true)
	private List<Comment> comments = new ArrayList<>();
	
	@JsonIgnore
	@OneToMany(mappedBy = "video", orphanRemoval = true)
	Set<WatchHistory> watchHistorySet = new HashSet<>();
	
	@Override
	public SearchType getType() {
		return SearchType.VIDEO;
	}
	
	@Override
	public ViewVideoDTO convertToViewVideoDTO(UserRepository userRepository) {
		return new ViewVideoDTO(this.videoId, this.title, this.category, this.description, this.URL,
				this.uploadDate, this.duration, this.numberOfLikes, this.numberOfDislikes, this.numberOfViews,
				userRepository.findById(this.uploaderId).get().getFullName(), this.comments.size());
	}
	
	@Override
	public SearchableVideoDTO convertToSearchableVideoDTO(UserRepository userRepository) {
		return new SearchableVideoDTO(this.videoId, this.title,
				userRepository.findById(this.uploaderId).get().getFullName(),
				this.uploaderId, this.isPrivate);
	}
}
