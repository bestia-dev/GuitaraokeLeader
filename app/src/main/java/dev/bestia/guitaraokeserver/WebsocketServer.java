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
    private final MainActivity main_activity;

    public WebsocketServer(int port, MainActivity activity) {
        super(new InetSocketAddress(port));
        connections = new HashSet<>();
        this.main_activity = activity;
        printLine("WebsocketServer on port "+port);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        Date date = new Date();
        printLine("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        printLine("Closed connection");
        connections.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // printLine("Message from client: " + message);
        Gson gson = new Gson();
        Message msg = gson.fromJson(message, MessageReceiver.class).toMessage();
        printMessage(msg);
        for (WebSocket sock : connections) {
            sock.send(message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        //ex.printStackTrace();
        if (conn != null) {
            printLine("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
            connections.remove(conn);
            // do some thing if required
        }
        printLine("Websocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        //printLine("onStart");
    }

    private void printMessage(Message msg) {
        this.main_activity.printMessage(msg.username,msg.timestamp,msg.data);
    }
    private void printLine(String string){
        this.main_activity.printMessage("server", new Date(), string);
    }
}
