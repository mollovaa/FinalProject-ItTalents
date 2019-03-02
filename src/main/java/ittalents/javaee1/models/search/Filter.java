package ittalents.javaee1.models.search;

public enum Filter {
	DATE_OF_UPLOAD("upload_date", SearchType.VIDEO), LENGTH_SHORT("length_short", SearchType.VIDEO),
	LENGTH_LONG("length_long", SearchType.VIDEO), VIEWS("views", SearchType.VIDEO),
	MOST_LIKES("likes", SearchType.VIDEO), VIDEOS("videos", SearchType.VIDEO),
	PLAYLISTS("playlists", SearchType.PLAYLIST), USERS("users", SearchType.USER);

	private final SearchType searchType;
	private final String name;

	Filter(String name, SearchType searchType) {
		this.searchType = searchType;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public SearchType getSearchType() {
		return searchType;
	}
}
