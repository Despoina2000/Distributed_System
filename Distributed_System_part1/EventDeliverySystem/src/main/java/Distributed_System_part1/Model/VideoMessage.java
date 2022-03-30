package Distributed_System_part1.Model;

import Distributed_System_part1.Util.VideoMetadata;

import java.util.ArrayList;

public class VideoMessage extends Message{
    private String contentType = "video";
    private VideoMetadata videoMetadata;
    private ArrayList<byte[]> content;


    public VideoMessage(String username, String topic, VideoMetadata videoMetadata, ArrayList<byte[]> content) {
        super(username, topic);
        this.videoMetadata = videoMetadata;
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public VideoMetadata getVideoMetadata() {
        return videoMetadata;
    }

    public ArrayList<byte[]> getContent() {
        return content;
    }
}
