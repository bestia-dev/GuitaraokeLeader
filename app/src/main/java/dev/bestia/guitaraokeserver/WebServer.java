package dev.bestia.guitaraokeserver;

import android.content.res.AssetManager;
import android.webkit.MimeTypeMap;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import fi.iki.elonen.NanoHTTPD;


public class WebServer extends NanoHTTPD {

    private final AssetManager assetManager;
    private final MainActivity main_activity;

    public WebServer(int port, AssetManager assetManager,MainActivity activity) {
        super(port);
        this.assetManager = assetManager;
        this.main_activity = activity;
    }

    /**
     * Selects the right file path to give as response.
     * This allows angular routing.
     * @param uri Requested URL
     * @return the requested file path or 'index.html'.
     */
    private  String getFilePath(String uri) {
        if (uri.startsWith("/css")) {
            try {
                String filename_from_uri = uri.substring(5);
                boolean fileExists = Arrays.asList(this.assetManager.list("dist/css")).contains(filename_from_uri);
                if (fileExists) {
                    return "css/"+filename_from_uri;
                }
            }catch(IOException e) {
                printLine(e.getMessage());
            }
        }
        if (uri.startsWith("/videos")) {
            try {
                String filename_from_uri = uri.substring(8);
                boolean fileExists = Arrays.asList(this.assetManager.list("dist/videos")).contains(filename_from_uri);
                if (fileExists) {
                    return "videos/"+filename_from_uri;
                }
            }catch(IOException e) {
                printLine(e.getMessage());
            }
        }
        try {
            boolean fileExists = Arrays.asList(this.assetManager.list("dist")).contains(uri.substring(1));
            if (fileExists) {
                return uri.substring(1);
            }
        }catch(IOException e) {
            printLine(e.getMessage());
        }
        return "index.html";
    }

    private String getMimeType(String file) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(file);
        // the extension for mp4 didn't come correct, maybe because of space - special characters
        // but otherwise it works just fine.
        if (file.endsWith(".mp4")){
            ext="mp4";
        }
        switch (ext) {
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "woff2":
                return "font/woff2";
            case "woff":
                return "font/woff";
            case "ttf":
                return "font/ttf";
            case "mp4":
                return "video/mp4";
            default:
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        }
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

    public static byte[] getBytes(InputStream is) throws IOException {

        int len;
        int size = 1024;
        byte[] buf;

        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
            if (len == -1){
                Log.d("myTag","len is -1");
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
        String filepath = getFilePath(session.getUri());
        String mimeType = getMimeType(filepath);
        String content;
        byte[] buffer;
        InputStream is;
        try {
            is = this.assetManager.open("dist/" + filepath);
            if (binaryResponse(mimeType)) {
                return newFixedLengthResponse(Response.Status.OK, mimeType, is, -1);
            }
            int size = is.available();
            buffer = new byte[size];
            int len = is.read(buffer);
            if (len==-1){
                Log.d("myTag","len = -1");
            }
            is.close();
            content = new String(buffer);
            // content = content.replace("old string", "new string");
        }catch(IOException e) {
            printLine("IOException (at serve): " + e.getMessage());
            mimeType = "text/html";
            content = "<html><body><h1>IOException</h1>\n<p>" + e.getMessage() + "</p>\n<p>Serving " + session.getUri() + " !</p></body></html>\n";
        }
        return newFixedLengthResponse(Response.Status.OK, mimeType, content);
    }
    private void printLine(String string){
        this.main_activity.printLine(string);
    }
}