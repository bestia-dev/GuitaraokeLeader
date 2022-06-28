package dev.bestia.guitaraokeleader;

import android.annotation.SuppressLint;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    String ip;
    WebServer webserver;
    WebsocketServer websocketserver;
    int WEB_SERVER_TCP_PORT = 8080;
    int WEB_SOCKET_TCP_PORT = 3000;
    StringBuilder server_text = new StringBuilder();
    String guitaraokeFolderUri;
    WebView web_view_1;
    SharedPreferences preferenceManager;
    DownloadManager download_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        download_manager = (DownloadManager) this.getSystemService  (Context.DOWNLOAD_SERVICE);
        // try with new approach
        ip = getIpAddress();
        //old function: ip = utils.getIP(this);
        if (ip == null) {
            printLine("Error: Please connect to wifi.");
            return;
        }

        chooseFolderOnEveryStart();
    }

    /// disable back button
    @Override
    public void onBackPressed() {

    }

    /// Every time the app starts it asks for a folder
    /// So the user can have multiple folders with different kind of music
    /// Ideally it will be /Music/Guitaraoke/romantic or /Music/Guitaraoke/rock
    public void chooseFolderOnEveryStart(){
        ActivityResultLauncher<Intent> launchChooseFolderFromStart = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        guitaraokeFolderUri = data.toUri(0);
                        afterFolderChoice();
                    }
                });
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        launchChooseFolderFromStart.launch(intent);
    }

    /// the OnCreate continues after the Folder choice
    @SuppressLint("SetJavaScriptEnabled")
    public void afterFolderChoice(){
        try {
            MainActivity context = MainActivity.this;
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            printLine("GuitaraokeLeader ver. "+ version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        web_view_1 =  findViewById(R.id.web_view_1);

        // buttons in the header
        TextView header_title = findViewById(R.id.header_title);
        TextView header_debug =  findViewById(R.id.header_debug);
        TextView header_exit =  findViewById(R.id.header_exit);
        TextView header_ip_port = findViewById(R.id.header_ip_port);

        header_title.setOnClickListener(view -> {
            // restart the webview
            web_view_1.loadUrl(ip+":"+  WEB_SERVER_TCP_PORT + "/leader.html");
        });

        header_debug.setOnClickListener(view -> {
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

        header_exit.setOnClickListener(view -> finishAndRemoveTask());

        copyOnceWelcomeVideoToExternalStorage("Welcome to Guitaraoke Leader.mp4");
        copyOnceWelcomeVideoToExternalStorage("Welcome to Guitaraoke Follower.mp4");

        // Main function
        printLine("Initializing server...");
        // Init server
        Utils utils = new Utils(getApplicationContext());
        webserver = new WebServer(WEB_SERVER_TCP_PORT, getAssets(),this);
        websocketserver = new WebsocketServer(WEB_SOCKET_TCP_PORT, this);
        try {
            webserver.chosen_folder=chosenFolder();
            webserver.content_resolver=contentResolver();
            webserver.start();
            websocketserver.setReuseAddr(true);
            websocketserver.start();
            printLine("Listening on " + ip + ":"+ WEB_SERVER_TCP_PORT);
            Resources res = getResources();
            String text = String.format(res.getString(R.string.print_ip_address_and_port),ip, WEB_SERVER_TCP_PORT);
            header_ip_port.setText(text);

            // WebView: open browser for leader inside a webview to avoid app sleep
            LinearLayout linearLayout =  findViewById(R.id.linearLayout);
            RelativeLayout contentLayout =  findViewById(R.id.contentLayout);
            // fullscreen is a long story in android web view
            web_view_1.setWebChromeClient(new FullScreenClient(linearLayout, contentLayout));
            web_view_1.clearCache(true);
            web_view_1.clearHistory();
            // without this, any change in url opens the browser.
            web_view_1.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    final Uri uri = request.getUrl();
                    view.loadUrl(uri.toString());
                    return false;
                }
            });
            web_view_1.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
                Log.w("w",contentDisposition);
                String file_name_url = url.substring(url.lastIndexOf("/")+1);
                String file_name = null;
                try {
                    file_name = URLDecoder.decode(file_name_url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                download_song(url, file_name);
            });

            WebSettings web_view_settings = web_view_1.getSettings();
            web_view_settings.setJavaScriptEnabled(true);
            web_view_1.loadUrl(ip+":"+  WEB_SERVER_TCP_PORT + "/leader.html");



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
            ip += "Something Wrong! " + e + "\n";
        }
        return ip;
    }

    public void printMessage(String username, Date timestamp, String data) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String showDate = sdf.format(timestamp);
        runOnUiThread(() -> server_text.append(username).append(" ").append(showDate).append(" ").append(data).append("\n"));
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

    /// chosen folder as DocumentFile
    public DocumentFile chosenFolder() {
        MainActivity context = MainActivity.this;
        Uri folder_tree_uri = Uri.parse(guitaraokeFolderUri);
        DocumentFile df = DocumentFile.fromTreeUri(context, folder_tree_uri);
        assert df != null;
        return df;
    }
    public ContentResolver contentResolver(){
        MainActivity context = MainActivity.this;
        return context.getContentResolver();
    }

    public void copyOnceWelcomeVideoToExternalStorage(String display_name) {
        DocumentFile found_file = chosenFolder().findFile(display_name);
        if (found_file == null) {
            Log.w("w","!welcome_external_file.exists()");
            String file_asset = "guitaraokewebapp/videos/"+display_name;
            AssetManager assetManager = getAssets();
            InputStream in = null;
            BufferedOutputStream out = null;
            try {
                in = assetManager.open(file_asset);
                DocumentFile new_file = chosenFolder().createFile("video/mp4", display_name);
                assert new_file != null;
                Uri new_file_uri = new_file.getUri();
                out = new BufferedOutputStream( contentResolver().openOutputStream(new_file_uri));
                FileUtils.copy(in, out);
            } catch (IOException e) {
                printLine("Failed to copy asset file: " + file_asset + " " + e);
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
            MainActivity context = MainActivity.this;
            Uri uri = Uri.parse(song_url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            context.registerReceiver(attachmentDownloadCompleteReceive, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            printLine("download song_url: " + song_url);
            printLine("download file_name: " + file_name);
            // download manager cannot download to the folder I want
            // first I will download to an intermediate folder
            // and then I will move the file to the chosen folder
            File dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File file = new File(dir,file_name);
            if(file.exists() ){
                boolean del = file.delete();
            }
            request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_MUSIC,file_name);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  // This will show notification on top when downloading the file.
            request.setTitle("Downloading song: "+ file_name); // Title for notification.
            request.setDescription("Download guitaraoke song.");

            long downloadId = download_manager.enqueue(request);

            //Save the request downloadId, edit() + commit()
            SharedPreferences.Editor PrefEdit = preferenceManager.edit();
            PrefEdit.putString("DOWNLOAD_ID_"+downloadId, file_name);
            PrefEdit.apply();
        }
        catch (Exception e) {
            printLine("error in download_song: " + e.getMessage());
            Log.e("e","error in download_song: " + e.getMessage());
        }
    }
    BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                // move downloaded file
                String file_name = preferenceManager.getString("DOWNLOAD_ID_"+downloadId,"");
                File dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                File from_file = new File(dir,file_name);
                if(from_file.exists() ){
                    DocumentFile to_file = chosenFolder().createFile("video/mp4",file_name);
                    assert to_file != null;
                    moveDownloadedFile(from_file, to_file);
                }
            }
        }
    };
    /// only mp4
    public void moveDownloadedFile(File from_file, DocumentFile to_file) {
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new FileInputStream(from_file);
            Uri new_file_uri = to_file.getUri();
            out = new BufferedOutputStream( contentResolver().openOutputStream(new_file_uri));
            FileUtils.copy(in, out);
        } catch (IOException e) {
            printLine("Failed to copy mp4 file: " + from_file.getName() + " " + e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                    // delete cached file
                    final boolean delete = from_file.delete();
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