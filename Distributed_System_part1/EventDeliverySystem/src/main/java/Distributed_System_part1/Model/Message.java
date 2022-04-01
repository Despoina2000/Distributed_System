package Distributed_System_part1.Model;

/**
 * basic message class
 */
public class Message {
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
}
