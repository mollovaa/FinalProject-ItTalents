package ittalents.javaee1.models.repository;

import ittalents.javaee1.models.pojo.Video;
import ittalents.javaee1.util.exceptions.VideoNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findAllByTitleContaining(String title);

    List<Video> findAllByTitleContainingAndDurationLessThanEqual(String title, long duration);

    List<Video> findAllByTitleContainingAndDurationGreaterThan(String title, long duration);

    default Video getByVideoId(long videoId) throws VideoNotFoundException {
        Optional<Video> video = this.findById(videoId);
        if (!video.isPresent()) {
            throw new VideoNotFoundException();
        }
        return video.get();
    }
}
