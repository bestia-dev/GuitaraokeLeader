package dev.bestia.guitaraokeleader;
import android.annotation.SuppressLint;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {

    String ip;
    WebServer webserver;
    WebsocketServer websocketserver;
    int WEB_SERVER_TCP_PORT = 8080;
    int WEB_SOCKET_TCP_PORT = 3000;
    StringBuilder server_text = new StringBuilder();
    WebView web_view_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView button_stop_server =  findViewById(R.id.button_stop);
        button_stop_server.setOnClickListener(view -> {
            // finish will call onDestroy where is stop servers
            finish();
        });
        TextView button_show_log =  findViewById(R.id.button_show);
        button_show_log.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            // size of dialog 90% of screen
            int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
            int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);
            alertDialogBuilder.setNeutralButton(R.string.close,null);
            alertDialogBuilder.setMessage(server_text.toString());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            alertDialog.getWindow().setLayout(width, height);
        });

        TextView header_ip_port = findViewById(R.id.header_ip_port);
        TextView header_title = findViewById(R.id.header_title);

        copyOnceWelcomeVideoToExternalStorage();
        // Main function
        printLine("Initializing server...");
        // Init server
        Utils utils = new Utils(getApplicationContext());
        // try with new approach
        ip = getIpAddress();
        //old function: ip = utils.getIP(this);
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

                // WebView: open browser for leader inside a webview to avoid app sleep
                web_view_1 =  findViewById(R.id.web_view_1);
                LinearLayout linearLayout =  findViewById(R.id.linearLayout);
                RelativeLayout contentLayout =  findViewById(R.id.contentLayout);
                // fullscreen is a long story in android web view
                web_view_1.setWebChromeClient(new FullScreenClient(linearLayout, contentLayout));
                // without this, any change in url opens the browser.
                web_view_1.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return false;
                    }
                });
                web_view_1.setDownloadListener(new DownloadListener()
                {
                    public void onDownloadStart(String url, String userAgent,String contentDisposition, String mimetype,long contentLength)
                    {
                        Log.w("w",contentDisposition);
                        String file_name_url = contentDisposition.split("filename\\*=UTF-8''")[1];
                        String file_name = null;
                        try {
                            file_name = URLDecoder.decode(file_name_url, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        download_song(url, file_name);
                    }
                });

                WebSettings web_view_settings = web_view_1.getSettings();
                web_view_settings.setJavaScriptEnabled(true);
                web_view_1.loadUrl(ip+":"+  WEB_SERVER_TCP_PORT + "/leader.html");

                header_title.setOnClickListener(view -> {
                    // restart the webview
                    web_view_1.loadUrl(ip+":"+  WEB_SERVER_TCP_PORT + "/leader.html");
                });

            }catch (IOException e) {
                Toast.makeText(getApplicationContext(), "IOException: " +  e.getMessage(), Toast.LENGTH_LONG).show();
            }catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Exception: " +  e.getMessage(), Toast.LENGTH_LONG).show();
            }
    }

    // try to resolve the ip address also wen using the hotspot
    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    public void printMessage(String username, Date timestamp, String data) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String showDate = sdf.format(timestamp);
        runOnUiThread(() -> {
            server_text.append(username + " " + showDate + " " + data + "\n");
        });
    }

    public void printLine(String content) {
        if (content == null){
            content="null";
        }
        printMessage("Server", new Date(), content);
    }
    public void onDestroy() {
        Log.w("w","onDestroy");
        try {
            web_view_1.clearCache(true);
            web_view_1.clearHistory();
            web_view_1.destroy();
            this.websocketserver.stop();
            this.webserver.closeAllConnections();
            this.webserver.stop();
        } catch ( InterruptedException e)  {
            e.printStackTrace();
        }
        finishAndRemoveTask();
        System.exit(0);
        super.onDestroy();
    }

    //folder inside Android/data/data/your_package/
    public File getExternalVideosFolder() {
        return getExternalFilesDir("videos");
    }

    public void copyOnceWelcomeVideoToExternalStorage() {
        // check if the file exists in External storage
        File videos_folder = getExternalVideosFolder();
        File welcome_external_file = new File(videos_folder, "Welcome to Guitaraoke Leader.mp4");
        if (!welcome_external_file.exists()) {
            Log.w("w","!welcome_external_file.exists()");
            String welcome_asset = "guitaraokewebapp/videos/Welcome to Guitaraoke Leader.mp4";
            AssetManager assetManager = getAssets();
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(welcome_asset);
                File outFile = welcome_external_file;
                outFile.setReadable(true);
                out = new FileOutputStream(outFile);
                Utils.copyFile(in, out);
            } catch (IOException e) {
                printLine("Failed to copy asset file: " + welcome_asset + " " + e.toString());
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

    private void download_song(String song_url, String file_name){
        try {
            DownloadManager download_manager = (DownloadManager) this.getSystemService  (Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(song_url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            printLine("download file_name: " + file_name);
            File old_file = new File(this.getExternalVideosFolder() + "/"+ file_name);
            if(old_file.exists()){
                old_file.delete();
            }
            request.setDestinationInExternalFilesDir(this,"videos",file_name);
            download_manager.enqueue(request);
        }
        catch (Exception e) {
            printLine("error in download_song: " + e.getMessage());
            Log.e("e","error in download_song: " + e.getMessage());
        }
    }
}

