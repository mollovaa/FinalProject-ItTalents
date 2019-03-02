package ittalents.javaee1.hibernate;

import ittalents.javaee1.models.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findAllByTitleContaining(String title);

    List<Video> findAllByTitleContainingAndDurationLessThanEqual(String title, long duration);

    List<Video> findAllByTitleContainingAndDurationGreaterThan(String title, long duration);
}
