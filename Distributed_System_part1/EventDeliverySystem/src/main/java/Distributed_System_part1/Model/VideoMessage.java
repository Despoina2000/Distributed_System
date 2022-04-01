package Distributed_System_part1.Model;

import Distributed_System_part1.Util.VideoMetadata;

import java.io.File;
import java.util.ArrayList;

/**
 * video message, extends Message
 */
public class VideoMessage extends Message{
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

    public String getContentType() {
        return contentType;
    }

    public VideoMetadata getVideoMetadata() {
        return videoMetadata;
    }

    public File getContent() {
        return content;
    }

    public ArrayList<byte[]> getChunkedContent() {
        return chunkedContent;
    }
}
