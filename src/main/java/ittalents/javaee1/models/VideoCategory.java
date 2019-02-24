package ittalents.javaee1.models;

import java.util.Arrays;


public enum VideoCategory {
    CARS_AND_VEHICLES, COMEDY, EDUCATION, ENTERTAINMENTS, GAMES, JOURNEYS_AND_EVENTS,
    MOVIES_AND_ANIMATIONS, MUSIC, NEWS_AND_POLITICS, PEOPLE_AND_BLOGS, PETS_AND_ANIMALS, SCIENCE_AND_TECHNOLOGIES,
    SPORT;



    public static boolean contains(VideoCategory category) {
        return Arrays.asList(values()).contains(category);
    }
}
