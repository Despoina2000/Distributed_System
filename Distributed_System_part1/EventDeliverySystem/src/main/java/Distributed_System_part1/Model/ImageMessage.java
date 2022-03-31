package Distributed_System_part1.Model;

import Distributed_System_part1.Util.ImageMetadata;

import java.io.File;
import java.util.ArrayList;

public class ImageMessage extends Message {

    private final String contentType = "image";
    private ImageMetadata imageMetadata;
    private File content;
    private ArrayList<byte[]> chunkedContent;

    public ImageMessage(String username, String topic, ImageMetadata imageMetadata) {
        super(username, topic);
        this.imageMetadata = imageMetadata;
    }

    /**
     * overloaded constructor mazi me to content
     * @param username
     * @param topic
     * @param imageMetadata
     * @param content
     */
    public ImageMessage(String username, String topic, ImageMetadata imageMetadata, File content) {
        super(username, topic);
        this.imageMetadata = imageMetadata;
        this.content = content;
    }

    /**
     * overloaded constructor mazi me to content se chunks (tha apothikevete ston broker etsi)
     * @param username
     * @param topic
     * @param imageMetadata
     * @param chunkedContent
     */
    public ImageMessage(String username, String topic, ImageMetadata imageMetadata, ArrayList<byte[]> chunkedContent) {
        super(username, topic);
        this.imageMetadata = imageMetadata;
        this.chunkedContent = chunkedContent;
    }

    public String getContentType() {
        return contentType;
    }

    public ImageMetadata getImageMetadata() {
        return imageMetadata;
    }

    public File getContent() {
        return content;
    }

    public ArrayList<byte[]> getChunkedContent() {
        return chunkedContent;
    }
}
