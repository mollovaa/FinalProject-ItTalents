package ittalents.javaee1.controllers;

class ResponseMessages {

    static final String SUCCESSFULLY_ADDED_VIDEO = "{\"msg\" : \"You have successfully added a video!\"}";
    static final String SUCCESSFULLY_REMOVED_VIDEO = "{\"msg\" : \"You have successfully removed a video!\"}";
    static final String SUCCESSFULLY_LIKED_VIDEO = "{\"msg\" : \"You have successfully liked a video!\"}";
    static final String SUCCESSFULLY_DISLIKED_VIDEO = "{\"msg\" : \"You have successfully disliked a video!\"}";
    static final String ALREADY_LIKED_VIDEO = "{\"error\" : \"You have already liked this video!\"}";
    static final String ALREADY_DISLIKED_VIDEO = "{\"error\" : \"You have already disliked this video!\"}";
    static final String SERVER_ERROR = "{\"error\" : \"Please try again later!\"}";
    static final String EXPIRED_SESSION = "{\"error\" : \"Please login to continue!\"}";
    static final String ACCESS_DENIED = "{\"error\" : \"Permitted operation\"}";
    static final String NOT_FOUND = "{\"error\" : \"Sorry, the resource is not found!\"}";
    static final String INVALID_VIDEO_TITLE = "{\"error\" : \"Invalid title!\"}";
    static final String INVALID_VIDEO_DURATION = "{\"error\" : \"Invalid duration!\"}";
    static final String INVALID_VIDEO_CATEGORY = "{\"error\" : \"Invalid category!\"}";
    static final String SUCCESSFULLY_CREATED_PLAYLIST = "{\"msg\" : \"You have successfully created a playlist!\"}";
    static final String SUCCESSFULLY_REMOVED_PLAYLIST = "{\"msg\" : \"You have successfully removed a playlist!\"}";
    static final String SUCCESSFULLY_ADDED_VIDEO_TO_PLAYLIST = "{\"msg\" : \"You have successfully added a video to playlist!\"}";

}
