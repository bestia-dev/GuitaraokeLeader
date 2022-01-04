package dev.bestia.guitaraokeleader;

import android.annotation.SuppressLint;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
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

    final String strPref_Download_ID = "PREF_DOWNLOAD_ID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        guitaraokeFolderUri = preferenceManager.getString("guitaraokeFolderUri",MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString());

        // region: on click for buttons
        ActivityResultLauncher<Intent> launchChooseFolderFromButton = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            assert data != null;
                            guitaraokeFolderUri = data.toUri(0);
                            preferenceManager.edit().putString("guitaraokeFolderUri", guitaraokeFolderUri);

                        }
                    }
                });
        // header_title will have on click later, after the web server started
        // With button_folder the user will choose the shared folder.
        // Ideally it will be /Movies/Guitaraoke, but he/she has to create it manually
        ActivityResultLauncher<Intent> launchChooseFolderFromStart = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            assert data != null;
                            guitaraokeFolderUri = data.toUri(0);
                            preferenceManager.edit().putString("guitaraokeFolderUri", guitaraokeFolderUri);
                            afterFolderChoice(launchChooseFolderFromButton);
                        }
                    }
                });

        //TODO: just for test ask for folder every time, because it does not store in preferences
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, guitaraokeFolderUri);
        launchChooseFolderFromStart.launch(intent);

    }

    public void afterFolderChoice(ActivityResultLauncher<Intent> launchChooseFolderFromButton ){

        // buttons in the header
        TextView header_title = findViewById(R.id.header_title);
        TextView button_folder =  findViewById(R.id.button_folder);
        TextView button_debug =  findViewById(R.id.button_debug);
        TextView header_ip_port = findViewById(R.id.header_ip_port);
        button_folder.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, guitaraokeFolderUri);
            launchChooseFolderFromButton.launch(intent);
        });

        button_debug.setOnClickListener(view -> {
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

        download_manager = (DownloadManager) this.getSystemService  (Context.DOWNLOAD_SERVICE);
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
            web_view_1.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
                Log.w("w",contentDisposition);
                String file_name_url = contentDisposition.split("filename\\*=UTF-8''")[1];
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

    //folder inside Android/data/data/your_package/
    public File getGuitaraokeFolder() {
        return new File(Uri.parse(guitaraokeFolderUri).getPath());
    }

    public void copyOnceWelcomeVideoToExternalStorage() {
        MainActivity context = MainActivity.this;
        ContentResolver content = context.getContentResolver();
        Uri folder_uri =Uri.parse(guitaraokeFolderUri);
        DocumentsContract.findDocumentPath()

        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                folder_uri, DocumentsContract.getTreeDocumentId(folder_uri));
        childrenUri.
        // check if the file exists in External storage

        DocumentFile folder = DocumentFile.fromSingleUri(context, folder_uri);
        folder.

        Uri welcome_external_uri = Uri.parse(guitaraokeFolderUri+"/Welcome to Guitaraoke Leader.mp4");
        DocumentFile welcome_external_file = DocumentFile.fromSingleUri(context, welcome_external_uri);
        if (!welcome_external_file.exists()) {
            Log.w("w","!welcome_external_file.exists()");
            String welcome_asset = "guitaraokewebapp/videos/Welcome to Guitaraoke Leader.mp4";
            AssetManager assetManager = getAssets();
            InputStream in = null;
            BufferedOutputStream out = null;
            try {
                in = assetManager.open(welcome_asset);
                out = new BufferedOutputStream( content.openOutputStream(welcome_external_uri));
                FileUtils.copy(in, out);
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
            Uri uri = Uri.parse(song_url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            printLine("download song_url: " + song_url);
            printLine("download file_name: " + file_name);
            File old_file = new File(this.getGuitaraokeFolder() + "/"+ file_name);
            if(old_file.exists()){
                old_file.delete();
            }
            request.setDestinationInExternalFilesDir(this,"videos",file_name);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  // This will show notification on top when downloading the file.
            request.setTitle("Downloading song: "+ file_name); // Title for notification.
            request.setDescription("Download guitaraoke song.");

            long id = download_manager.enqueue(request);

            //Save the request id
            SharedPreferences.Editor PrefEdit = preferenceManager.edit();
            PrefEdit.putLong(strPref_Download_ID, id);
            PrefEdit.commit();
        }
        catch (Exception e) {
            printLine("error in download_song: " + e.getMessage());
            Log.e("e","error in download_song: " + e.getMessage());
        }
    }

    private void CheckDownloadStatus(){
        DownloadManager.Query query = new DownloadManager.Query();
        long id = preferenceManager.getLong(strPref_Download_ID, 0);
        query.setFilterById(id);
        Cursor cursor = download_manager.query(query);
        if(cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int reason = cursor.getInt(columnReason);

            switch(status){
                case DownloadManager.STATUS_FAILED:
                    String failedReason = "";
                    switch(reason){
                        case DownloadManager.ERROR_CANNOT_RESUME:
                            failedReason = "ERROR_CANNOT_RESUME";
                            break;
                        case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                            failedReason = "ERROR_DEVICE_NOT_FOUND";
                            break;
                        case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                            failedReason = "ERROR_FILE_ALREADY_EXISTS";
                            break;
                        case DownloadManager.ERROR_FILE_ERROR:
                            failedReason = "ERROR_FILE_ERROR";
                            break;
                        case DownloadManager.ERROR_HTTP_DATA_ERROR:
                            failedReason = "ERROR_HTTP_DATA_ERROR";
                            break;
                        case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                            failedReason = "ERROR_INSUFFICIENT_SPACE";
                            break;
                        case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                            failedReason = "ERROR_TOO_MANY_REDIRECTS";
                            break;
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                            failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                            break;
                        case DownloadManager.ERROR_UNKNOWN:
                            failedReason = "ERROR_UNKNOWN";
                            break;
                    }

                    Toast.makeText(MainActivity.this,
                            "FAILED: " + failedReason,
                            Toast.LENGTH_LONG).show();
                    break;
                case DownloadManager.STATUS_PAUSED:
                    String pausedReason = "";

                    switch(reason){
                        case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                            pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                            break;
                        case DownloadManager.PAUSED_UNKNOWN:
                            pausedReason = "PAUSED_UNKNOWN";
                            break;
                        case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                            pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                            break;
                        case DownloadManager.PAUSED_WAITING_TO_RETRY:
                            pausedReason = "PAUSED_WAITING_TO_RETRY";
                            break;
                    }

                    Toast.makeText(MainActivity.this,
                            "PAUSED: " + pausedReason,
                            Toast.LENGTH_LONG).show();
                    break;
                case DownloadManager.STATUS_PENDING:
                    Toast.makeText(MainActivity.this,
                            "PENDING",
                            Toast.LENGTH_LONG).show();
                    break;
                case DownloadManager.STATUS_RUNNING:
                    Toast.makeText(MainActivity.this,
                            "RUNNING",
                            Toast.LENGTH_LONG).show();
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:

                    Toast.makeText(MainActivity.this,
                            "SUCCESSFUL",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}

