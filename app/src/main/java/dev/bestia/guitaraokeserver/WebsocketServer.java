package dev.bestia.guitaraokeserver;

import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class WebsocketServer extends WebSocketServer {

    private final Set<WebSocket> connections;
    private final ChatActivity activity;

    public WebsocketServer(int port, ChatActivity activity) {
        super(new InetSocketAddress(port));
        connections = new HashSet<>();
        this.activity = activity;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        Date date = new Date();
        Message msg = new Message(conn.getRemoteSocketAddress().getAddress().getHostAddress() ,date,"onOpen");
        addMessage(msg);
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);
        Gson gson = new Gson();
        Message msg = gson.fromJson(message, MessageReceiver.class).toMessage();
        addMessage(msg);
        for (WebSocket sock : connections) {
            sock.send(message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        //ex.printStackTrace();
        if (conn != null) {
            System.out.println("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
            connections.remove(conn);
            // do some thing if required
        }
        System.out.println("Websocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
    }

    private void addMessage(Message msg) {
        this.activity.addMessage(msg);
    }
}
