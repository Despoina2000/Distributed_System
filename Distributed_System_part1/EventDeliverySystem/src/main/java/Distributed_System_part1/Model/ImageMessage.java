package Distributed_System_part1.Model;

import Distributed_System_part1.Util.ImageMetadata;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * image message, extends Message
 */
public class ImageMessage extends Message implements Serializable {

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

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ImageMetadata getMetadata() {
        return imageMetadata;
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
        return "ImageMessage{" +
                "username='" + username + '\'' +
                ", topic='" + topic + '\'' +
                ", contentType='" + contentType + '\'' +
                ", imageMetadata=" + imageMetadata +
                '}';
    }
}
