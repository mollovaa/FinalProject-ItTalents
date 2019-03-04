package ittalents.javaee1.repository;

import ittalents.javaee1.models.pojo.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Video getByVideoId(long videoId);

    List<Video> findAllByTitleContaining(String title);

    List<Video> findAllByTitleContainingAndDurationLessThanEqual(String title, long duration);

    List<Video> findAllByTitleContainingAndDurationGreaterThan(String title, long duration);
}
