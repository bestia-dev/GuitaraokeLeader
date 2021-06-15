package dev.bestia.guitaraokeleader;

import android.content.res.AssetManager;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import fi.iki.elonen.NanoHTTPD;

// This WebServer is customized for Guitaraoke.
// It does not cover many generic use-case, but only specific for this project.

public class WebServer extends NanoHTTPD {

    private final AssetManager assetManager;
    private final MainActivity main_activity;
    long sync_clock_received_timestamp;

    public WebServer(int port, AssetManager assetManager,MainActivity activity) {
        super(port);
        this.assetManager = assetManager;
        this.main_activity = activity;
    }

    private  String getFilePath(String uri) {
        // All files are in Assets, except /videos are in ExternalStorage.
        if (uri.startsWith("/videos")) {
            String filename_from_uri = uri.substring(8);
            boolean fileExists = Arrays.asList(main_activity.getExternalVideosFolder().list()).contains(filename_from_uri);
            if (fileExists) {
                return "videos/"+filename_from_uri;
            }
            else{
                for (File file : main_activity.getExternalVideosFolder().listFiles()){
                    printLine("file: "+file.getName());
                }
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

    public byte[] getBytes(InputStream is) throws IOException {

        int len;
        int size = 1024;
        byte[] buf;

        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
            if (len == -1){
                printLine("len is -1");
            }
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }
        return buf;
    }

    @Override
    public Response serve(IHTTPSession session) {
        sync_clock_received_timestamp = System.currentTimeMillis();
        String content;
        String mimeType="text/html";
        // files are in assets/guitaraokewebapp/
        // except videos are in externalStorage. I will enable to manually download videos from urls.
        String filepath = getFilePath(session.getUri());
        mimeType = getMimeType(filepath);

        byte[] buffer;
        InputStream is;
        try {
            if (filepath.startsWith("videos/")) {
                String path = main_activity.getExternalFilesDir("").getPath() + "/" + filepath;
                File file = new File(path);
                is = new FileInputStream(file);
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
            content = dynamic_content(filepath, content, session);

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
    private String dynamic_content(String filepath, String content,IHTTPSession session){
        if (filepath.equals("leader.html")) {
            String new_html = leader_html_list_of_songs();
            content = content.replace("<!--list_of_files_in_folder_videos-->", new_html);
        }
        return content;
    }
    private String leader_html_list_of_songs(){
        StringBuilder new_html=new StringBuilder();
        for (File file:main_activity.getExternalVideosFolder().listFiles()){
            String song_name = file.getName().replace(" - guitaraoke.mp4","").replace(".mp4","");
            new_html.append("<div class='class_song_name'>"+ Utils.escapeHtml(song_name)+"</div>\n");
        }
        return new_html.toString();
    }
    // endregion: processing dynamic content
}