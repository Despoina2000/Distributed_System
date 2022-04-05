package Distributed_System_part1.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * text message, extends Message
 */
public class TextMessage extends Message implements Serializable {
    private String content;
    private String contentType = "text";

    public TextMessage(String username, String topic, String content) {
        super(username, topic);
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String toString() {
        return "TextMessage{" +
                "username='" + username + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public <T> T getMetadata() {
        return null;
    }

    @Override
    public ArrayList<byte[]> getChunkedContent() {
        return null;
    }
}
