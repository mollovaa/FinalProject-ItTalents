package ittalents.javaee1.util;

import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.models.pojo.Video;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class StorageManager {
    private static String VIDEO_DIRS = "src/main/resources/videos";

    //Return the video url that should be set after
    public String uploadVideo(MultipartFile multipartFile, Video video) throws IOException {
        String videoName =
                (video.getUploaderId() + "-" + System.currentTimeMillis() + ".mp4").replaceAll(",", "");

        String pathToVideo = VIDEO_DIRS + File.separator + video.getUploaderId();
        String videoDir = VIDEO_DIRS + File.separator + video.getUploaderId() + File.separator + videoName;
        convertMultiPartToFile(multipartFile, videoDir, pathToVideo);
        return videoName;
    }

    private File convertMultiPartToFile(MultipartFile file, String videoDir, String pathToVideo) throws IOException {
        new File(pathToVideo).mkdirs();  // makes directories if not exist

        File convFile = new File(videoDir);
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public void deleteVideo(Video video) {
        File videoFile = new File(VIDEO_DIRS + File.separator + video.getUploaderId()
                + File.separator + video.getURL());
        videoFile.delete();
    }

    public void deleteFolder(User user) {
        File videoFile = new File(VIDEO_DIRS + File.separator + user.getUserId());
        videoFile.delete();
    }

    // returns bytes read from the file saved on the url
    public byte[] downloadVideo(Video video) throws IOException {
        File newVideo = new File(VIDEO_DIRS + File.separator + video.getUploaderId()
                + File.separator + video.getURL());
        FileInputStream fis = new FileInputStream(newVideo);
        byte[] bytes = fis.readAllBytes();
        fis.close();
        return bytes;
    }


}
