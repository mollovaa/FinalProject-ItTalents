package ittalents.javaee1.util.exceptions;

public class SearchNotFoundException extends BadRequestException {

    private static final String THE_SEARCH_DOES_NOT_EXIST = "The search does not exist!";

    public SearchNotFoundException() {
        super(THE_SEARCH_DOES_NOT_EXIST);
    }
}
