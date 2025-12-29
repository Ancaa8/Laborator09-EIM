package ro.pub.cs.systems.eim.lab09.chatservicejmdns.model;

public class Message {

    private final String content;
    private final int type;

    public Message(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

}
