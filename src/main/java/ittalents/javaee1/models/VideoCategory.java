package ittalents.javaee1.models;

import java.util.Arrays;
import java.util.stream.Collectors;


public enum VideoCategory {
    CARS_AND_VEHICLES, COMEDY, EDUCATION, ENTERTAINMENTS, GAMES, JOURNEYS_AND_EVENTS,
    MOVIES_AND_ANIMATIONS, MUSIC, NEWS_AND_POLITICS, PEOPLE_AND_BLOGS, PETS_AND_ANIMALS, SCIENCE_AND_TECHNOLOGIES,
    SPORT;


    public static boolean contains(String category) {
        return Arrays.stream(values())
                .map(VideoCategory::name)
                .collect(Collectors.toList())
                .contains(category.toUpperCase());
    }
}
