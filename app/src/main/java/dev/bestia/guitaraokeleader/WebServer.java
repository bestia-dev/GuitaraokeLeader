package dev.bestia.guitaraokeleader;

import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import fi.iki.elonen.NanoHTTPD;

// This WebServer is customized for Guitaraoke.
// It does not cover many generic use-case, but only specific for this project.

public class WebServer extends NanoHTTPD {

    private final AssetManager assetManager;
    private final MainActivity main_activity;
    long sync_clock_received_timestamp;
    DocumentFile chosen_folder;
    ContentResolver content_resolver;

    public WebServer(int port, AssetManager assetManager,MainActivity activity) {
        super(port);
        this.assetManager = assetManager;
        this.main_activity = activity;
    }

    private  String getFilePath(String uri) {
        // All files are in Assets, except /videos are in ExternalStorage.
        if (uri.startsWith("/videos")) {
            String filename_from_uri = uri.substring(8);
            DocumentFile found_file = chosen_folder.findFile(filename_from_uri);
            if (found_file != null) {
                return "videos/"+filename_from_uri;
            }
        }
        if (uri.startsWith("/css")) {
            try {
                String filename_from_uri = uri.substring(5);
                Log.i("","fileExists: "+filename_from_uri);
                boolean fileExists = Arrays.asList(this.assetManager.list("guitaraokewebapp/css")).contains(filename_from_uri);
                if (fileExists) {
                    return "css/"+filename_from_uri;
                }
            }catch(IOException e) {
                printLine(e.getMessage());
            }
        }
        if (uri.startsWith("/js")) {
            try {
                String filename_from_uri = uri.substring(4);
                boolean fileExists = Arrays.asList(this.assetManager.list("guitaraokewebapp/js")).contains(filename_from_uri);
                if (fileExists) {
                    return "js/"+filename_from_uri;
                }
            }catch(IOException e) {
                printLine(e.getMessage());
            }
        }
        try {
            boolean fileExists = Arrays.asList(this.assetManager.list("guitaraokewebapp")).contains(uri.substring(1));
            if (fileExists) {
                return uri.substring(1);
            }
        }catch(IOException e) {
            printLine(e.getMessage());
        }
        return "index.html";
    }

    private String getMimeType(String file_path) {
        // extensions are (simplistically) the last part of the filename delimiter with a dot.
        if (file_path.endsWith(".html")){
            return "text/html";
        } else if (file_path.endsWith(".css")){
            return "text/css";
        } else if (file_path.endsWith(".js")){
            return "application/javascript";
        } else if (file_path.endsWith(".mp4")){
            return "video/mp4";
        } else if (file_path.endsWith(".woff2")){
            return "font/woff2";
        } else if (file_path.endsWith(".woff")){
            return "font/woff";
        } else if (file_path.endsWith(".ttf")){
            return "font/ttf";
        } else if (file_path.endsWith(".ico")){
        return "image/x-icon";
        } else if (file_path.endsWith(".jpg")){
            return "image/jpeg";
        } else if (file_path.endsWith(".png")){
            return "image/png";
    }
        return "";
    }

    private static boolean binaryResponse(String mimeType) {
        if (mimeType == null) {
            mimeType = "";
        }
        switch (mimeType) {
            case "text/html":
            case "application/javascript":
            case "text/css":
            case "":
                return false;
        }
        return true;
    }

    @Override
    public Response serve(IHTTPSession session) {
        sync_clock_received_timestamp = System.currentTimeMillis();
        String content;
        String mimeType;
        // files are in assets/guitaraokewebapp/
        // except videos are in externalStorage. I will enable to manually download videos from urls.
        String filepath = getFilePath(session.getUri());
        mimeType = getMimeType(filepath);

        byte[] buffer;
        InputStream is;
        try {
            if (filepath.startsWith("videos/")) {
                String display_name = filepath.substring(7);
                DocumentFile found_file = chosen_folder.findFile(display_name);
                assert found_file != null;
                Uri found_file_uri = found_file.getUri();
                is = new BufferedInputStream(content_resolver.openInputStream(found_file_uri));
            } else {
                is = this.assetManager.open("guitaraokewebapp/" + filepath);
            }
            if (binaryResponse(mimeType)) {
                return newFixedLengthResponse(Response.Status.OK, mimeType, is, -1);
            }
            int size = is.available();
            buffer = new byte[size];
            int len = is.read(buffer);
            if (len == -1) {
                printLine("len = -1");
            }
            is.close();
            content = new String(buffer);

            // modify static files with dynamic content
            content = dynamic_content(filepath, content);

        } catch (IOException e) {
            printLine("IOException (at serve): " + e.getMessage());
            mimeType = "text/html";
            content = "<html><body><h1>IOException</h1>\n<p>" + e.getMessage() + "</p>\n<p>Serving " + session.getUri() + " !</p></body></html>\n";
        }
        return newFixedLengthResponse(Response.Status.OK, mimeType, content);
    }
    private void printLine(String string){
        this.main_activity.printLine(string);
    }
    // region: processing dynamic content
    private String dynamic_content(String filepath, String content){
        if (filepath.equals("leader.html")) {
            String new_html = leader_html_list_of_songs();
            content = content.replace("<!--list_of_files_in_folder_videos-->", new_html);
        }
        return content;
    }
    private String leader_html_list_of_songs(){
        StringBuilder new_html=new StringBuilder();
        for (DocumentFile file:chosen_folder.listFiles()){
            if (file.getName().endsWith(".mp4")){
            String song_url = "videos/"+file.getName();
            String song_name = Objects.requireNonNull(file.getName()).replace(" - guitaraoke.mp4","").replace(".mp4","");
            new_html.append("<div class='class_song_name ripple' data-url=\"")
                    .append(Utils.escapeHtml(song_url))
                    .append("\" >")
                    .append(Utils.escapeHtml(song_name))
                    .append("</div>\n");
        }
        }
        return new_html.toString();
    }
    // endregion: processing dynamic content
}