package dev.bestia.guitaraokeserver;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerActivity extends AppCompatActivity {

    String ip;
    WebServer webserver;
    WebsocketServer websocketserver;
    TextView HeaderIpPort;
    TextView text_view_1;
    ScrollView scroll_view_1;
    Button button_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        // Initialize variables (ListView)
        text_view_1 =(TextView)findViewById(R.id.text_view_1);
        scroll_view_1 =(ScrollView)findViewById(R.id.scroll_view_1);
        Button button_stop = (Button) findViewById(R.id.button_stop);
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop both servers
                finishAffinity();
            }
        });

        // Main function
        printLine("Initializing server...");
        // Init server
        Utils utils = new Utils(getApplicationContext());
        ip = utils.getIP();
        if (ip == null) {
            printLine("Error: Please connect to wifi.");
            return;
        }
        int WEB_SERVER_TCP_PORT = 8080;
        webserver = new WebServer(WEB_SERVER_TCP_PORT, getAssets(),this);
        int WEB_SOCKET_TCP_PORT = 3000;
        websocketserver = new WebsocketServer(WEB_SOCKET_TCP_PORT, this);
        try {
            webserver.start();
            websocketserver.start();
            printLine("Listening on " + ip + ":"+ WEB_SERVER_TCP_PORT);
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

    public void printMessage(String username, Date timestamp, String data) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String showDate = sdf.format(timestamp);
        runOnUiThread(() -> {
            text_view_1.append(username + " " + showDate + " " + data + "\n");
            scroll_view_1.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    private void printLine(String content) {
        printMessage("Server", new Date(), content);
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

