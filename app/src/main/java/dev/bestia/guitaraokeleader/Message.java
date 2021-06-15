package dev.bestia.guitaraokeleader;

import com.google.gson.Gson;

import java.util.Date;

public class Message {
    final String username;
    final Date timestamp;
    final String data;

    public Message(String username, Date timestamp, String data) {
        this.username = username;
        this.timestamp = timestamp;
        this.data = data;
    }
    public String toString(){
        Gson gson = new Gson();
        String msg = gson.toJson(this);
        return msg;
    }
}

/**
 * Message with properties as String
 */
class MessageReceiver {
    final String username;
    final String timestamp;
    final String data;

    /**
     * Builds a MessageReceiver Object. Use when parsing json.
     * @param username Username
     * @param timestamp Date in milliseconds
     * @param data Data
     */
    public MessageReceiver(String username, String timestamp, String data) {
        this.username = username;
        this.timestamp = timestamp;
        this.data = data;
    }
    public Message toMessage() {
        return new Message(this.username, new Date(Long.parseLong(this.timestamp)), this.data);
    }
}