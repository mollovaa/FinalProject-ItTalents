
package ittalents.javaee1.hibernate;

import ittalents.javaee1.models.User;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    boolean existsByVideoAndUser(Video video, User user);

    List<WatchHistory> getAllByUser(User user);

    List<WatchHistory> getAllByUserOrderByDateAsc(User user);

    List<WatchHistory> getAllByUserOrderByDateDesc(User user);

    WatchHistory getByUserAndVideo(User user, Video video);

}
