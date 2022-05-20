package Distributed_System_part2.app.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * basic message class
 */
public abstract class Message implements Serializable {
    protected String username;
    protected String topic;

    public Message(String username, String topic) {
        this.username = username;
        this.topic = topic;
    }

    public String getUsername() {
        return username;
    }

    public String getTopic() {
        return topic;
    }

    public abstract String getContentType();

    public abstract <T> T getMetadata();

    public abstract <T> T getContent();

    public abstract ArrayList<byte[]> getChunkedContent();
}
