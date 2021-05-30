package dev.bestia.guitaraokeserver;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    MessageAdapter adapter;
    ListView list;
    String ip;
    WebServer webserver;
    WebsocketServer websocketserver;
    TextView HeaderIpPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Initialize variables (ListView)
        list =  findViewById(R.id.messages_view);
        adapter = new MessageAdapter(new ArrayList<>(), getApplicationContext());
        list.setAdapter(adapter);


        // Main function
        serverMessage("Initializing server...");
        // Init server
        Utils utils = new Utils(getApplicationContext());
        ip = utils.getIP();
        if (ip == null) {
            serverMessage("Error: Please connect to wifi.");
            return;
        }
        int WEB_SERVER_TCP_PORT = 8080;
        webserver = new WebServer(WEB_SERVER_TCP_PORT, getAssets());
        int WEB_SOCKET_TCP_PORT = 3000;
        websocketserver = new WebsocketServer(WEB_SOCKET_TCP_PORT, this);
        try {
            webserver.start();
            websocketserver.start();
            serverMessage("Listening on " + ip + ":"+ WEB_SERVER_TCP_PORT);
            HeaderIpPort = (TextView)findViewById(R.id.headerIpPort);
            Resources res = getResources();
            String text = String.format(res.getString(R.string.print_ip_address_and_port),ip, WEB_SERVER_TCP_PORT);
            HeaderIpPort.setText( text);
        }catch (IOException e) {
            Toast.makeText(getApplicationContext(), "IOException: " +  e.getMessage(), Toast.LENGTH_LONG).show();
        }catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Exception: " +  e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void addMessage(Message msg) {
        final Message message = msg;
        runOnUiThread(() -> {
            adapter.add(message);
            adapter.notifyDataSetChanged();
            list.setSelection(adapter.getCount()-1);
        });
    }

    private void serverMessage(String content) {
        addMessage(new Message("Server", new Date(), content));
    }

    public void onDestroy() {
        try {
            this.webserver.closeAllConnections();
            this.webserver.stop();
            this.websocketserver.stop();
        } catch ( InterruptedException e)  {
            e.printStackTrace();
        }
        super.onDestroy();
    }

}

