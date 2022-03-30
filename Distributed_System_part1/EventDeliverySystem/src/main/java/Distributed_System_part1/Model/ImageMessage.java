package Distributed_System_part1.Model;

import Distributed_System_part1.Util.ImageMetadata;

import java.util.ArrayList;

public class ImageMessage extends Message {

    private String contentType = "image";
    private ImageMetadata imageMetadata;
    private ArrayList<byte[]> content;

    public ImageMessage(String username, String topic, ImageMetadata imageMetadata, ArrayList<byte[]> content) {
        super(username, topic);
        this.imageMetadata = imageMetadata;
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public ImageMetadata getImageMetadata() {
        return imageMetadata;
    }

    public ArrayList<byte[]> getContent() {
        return content;
    }
}
