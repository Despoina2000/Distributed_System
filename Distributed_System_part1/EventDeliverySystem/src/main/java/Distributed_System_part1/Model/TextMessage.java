package Distributed_System_part1.Model;

public class TextMessage extends Message {
    private String content;
    private String contentType = "text";

    public TextMessage(String username, String topic, String content) {
        super(username, topic);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

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
}
