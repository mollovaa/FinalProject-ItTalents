package ittalents.javaee1.models;

import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Video implements Searchable {
	
	private long videoId;
	private String title;
	private VideoCategory category;
	private String description;
	private LocalDate uploadDate;
	private Duration duration;    //seconds
	private int numberOfLikes;
	private int numberOfDislikes;
	private long numberOfViews;
	private long uploaderId;
	
	@Override
	public SearchType getType() {
		return SearchType.VIDEO;
	}
}
