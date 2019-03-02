package ittalents.javaee1.models.search;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Searchable {

	@JsonIgnore
	SearchType getType();
}
