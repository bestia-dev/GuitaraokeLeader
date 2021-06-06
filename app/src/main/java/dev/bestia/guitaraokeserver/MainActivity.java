package dev.bestia.guitaraokeserver;
import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    String ip;
    WebServer webserver;
    WebsocketServer websocketserver;
    TextView text_view_1;
    ScrollView scroll_view_1;
    int WEB_SERVER_TCP_PORT = 8080;
    int WEB_SOCKET_TCP_PORT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize variables (ListView)
        text_view_1 =findViewById(R.id.text_view_1);
        scroll_view_1 =findViewById(R.id.scroll_view_1);
        TextView button_stop_server =  findViewById(R.id.button_stop);
        button_stop_server.setOnClickListener(view -> {
            // stop web server and exit app
            try {
                websocketserver.stop();
            } catch (InterruptedException e) {
                printLine("InterruptedException");
                e.printStackTrace();
            }
            webserver.closeAllConnections();
            webserver.stop();
            finishAffinity();
            finish();
            System.exit(0);
        });
        TextView header_ip_port = findViewById(R.id.header_ip_port);
        TextView header_title = findViewById(R.id.header_title);


        copyOnceAssetsVideosToExternalStorage();
        // Main function
        printLine("Initializing server...");
        // Init server
        Utils utils = new Utils(getApplicationContext());
        ip = utils.getIP(this);
        if (ip == null) {
            printLine("Error: Please connect to wifi.");
            return;
        }
        webserver = new WebServer(WEB_SERVER_TCP_PORT, getAssets(),this);
            websocketserver = new WebsocketServer(WEB_SOCKET_TCP_PORT, this);
            try {
                webserver.start();
                websocketserver.setReuseAddr(true);
                websocketserver.start();
                printLine("Listening on " + ip + ":"+ WEB_SERVER_TCP_PORT);
                Resources res = getResources();
                String text = String.format(res.getString(R.string.print_ip_address_and_port),ip, WEB_SERVER_TCP_PORT);
                header_ip_port.setText( text);
                header_ip_port.setOnClickListener(view -> {openBrowserForLeader();});
                header_title.setOnClickListener(view -> {openBrowserForLeader();});
                // open browser for leader on start
                openBrowserForLeader();
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

    public void printLine(String content) {
        if (content == null){
            content="null";
        }
        printMessage("Server", new Date(), content);
    }
    public void openBrowserForLeader(){
        String url = "http://"+ip+":"+WEB_SERVER_TCP_PORT+"/leader.html";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
    public void onDestroy() {
        try {
            this.websocketserver.stop();
            this.webserver.closeAllConnections();
            this.webserver.stop();
        } catch ( InterruptedException e)  {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    //folder inside Android/data/data/your_package/
    public File getExternalVideosFolder() {
        return getExternalFilesDir("videos");
    }

    public void copyOnceAssetsVideosToExternalStorage() {
        // check if the folder exists in External storage
        String asset_folder = "guitaraoke_client/videos";
        File videos_folder = getExternalVideosFolder();
        if (videos_folder.listFiles().length == 0) {
            AssetManager assetManager = getAssets();
            String[] files = null;
            try {
                files = assetManager.list(asset_folder);
            } catch (IOException e) {
                printLine("Failed to get asset file list."+ e.toString() );
            }
            if (files != null) for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(asset_folder+filename);
                    File outFile = new File(videos_folder, filename);
                    out = new FileOutputStream(outFile);
                    Utils.copyFile(in, out);
                } catch (IOException e) {
                    printLine("Failed to copy asset file: " + filename+" "+ e.toString());
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            }
        }
    }

}

