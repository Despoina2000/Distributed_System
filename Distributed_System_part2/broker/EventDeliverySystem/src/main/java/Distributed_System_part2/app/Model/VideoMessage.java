package Distributed_System_part2.app.Model;

import Distributed_System_part2.app.Util.VideoMetadata;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * video message, extends Message
 */
public class VideoMessage extends Message implements Serializable {
    private final String contentType = "video";
    private VideoMetadata videoMetadata;
    private File content;
    private ArrayList<byte[]> chunkedContent;


    public VideoMessage(String username, String topic, VideoMetadata videoMetadata) {
        super(username, topic);
        this.videoMetadata = videoMetadata;
    }

    /**
     * overloaded constructor mazi me to content file
     * @param username
     * @param topic
     * @param videoMetadata
     * @param content
     */
    public VideoMessage(String username, String topic, VideoMetadata videoMetadata, File content) {
        super(username, topic);
        this.videoMetadata = videoMetadata;
        this.content = content;
    }

    /**
     * overloaded constructor mazi me to content se chunks (tha apothikevete ston broker etsi)
     * @param username
     * @param topic
     * @param videoMetadata
     * @param chunkedContent
     */
    public VideoMessage(String username, String topic, VideoMetadata videoMetadata, ArrayList<byte[]> chunkedContent) {
        super(username, topic);
        this.videoMetadata = videoMetadata;
        this.chunkedContent = chunkedContent;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public VideoMetadata getMetadata() {
        return videoMetadata;
    }

    @Override
    public File getContent() {
        return content;
    }

    @Override
    public ArrayList<byte[]> getChunkedContent() {
        return chunkedContent;
    }

    @Override
    public String toString() {
        return "VideoMessage{" +
                "username='" + username + '\'' +
                ", topic='" + topic + '\'' +
                ", contentType='" + contentType + '\'' +
                ", videoMetadata=" + videoMetadata +
                '}';
    }
}
