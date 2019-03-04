package ittalents.javaee1.models.search;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentFilter {

    NEWEST("newest"), TOP_COMMENTS("top");
    private final String filter;

    public static CommentFilter getCommentFilter(String filter) {
        CommentFilter[] allFilters = CommentFilter.values();
        for (int i = 0; i < allFilters.length; i++) {
            if (filter.equals(allFilters[i].getFilter())) {
                return CommentFilter.values()[i];
            }
        }
        return null;
    }
}