package dev.bestia.guitaraokeleader;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static android.content.Context.WIFI_SERVICE;

public class Utils {


    final Context mContext;

    public Utils(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Convert byte array to hex string
     * @param bytes toConvert
     * @return hexValue
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder string_builder = new StringBuilder();
        for (byte aByte : bytes) {
            int intVal = aByte & 0xff;
            if (intVal < 0x10) string_builder.append("0");
            string_builder.append(Integer.toHexString(intVal).toUpperCase());
        }
        return string_builder.toString();
    }

    /**
     * Get utf8 byte array.
     * @param str which to be converted
     * @return  array of NULL if error was found
     */
    public static byte[] getUTF8Bytes(String str) {
        try { return str.getBytes(StandardCharsets.UTF_8); } catch (Exception ex) { return null; }
    }

    /**
     * Load UTF8withBOM or any ansi text file.
     * @param filename which to be converted to string
     * @return String value of File
     * @throws java.io.IOException if error occurs
     */
    public static String loadFileAsString(String filename) throws java.io.IOException {
        final int BUFFER_LEN=1024;
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFFER_LEN)) {
            ByteArrayOutputStream byte_array_output_stream = new ByteArrayOutputStream(BUFFER_LEN);
            byte[] bytes = new byte[BUFFER_LEN];
            boolean isUTF8 = false;
            int read, count = 0;
            while ((read = is.read(bytes)) != -1) {
                if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    isUTF8 = true;
                    byte_array_output_stream.write(bytes, 3, read - 3); // drop UTF8 bom marker
                } else {
                    byte_array_output_stream.write(bytes, 0, read);
                }
                count += read;
            }
            return isUTF8 ? new String(byte_array_output_stream.toByteArray(), StandardCharsets.UTF_8) : new String(byte_array_output_stream.toByteArray());
        }
    }

    /**
     * Returns MAC address of the given interface name.
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return  mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface interface_1 : interfaces) {
                if (interfaceName != null) {
                    if (!interface_1.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = interface_1.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) buf.append(String.format("%02X:",aMac));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface interface_2 : interfaces) {
                List<InetAddress> addresses = Collections.list(interface_2.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress()) {
                        String host_address = address.getHostAddress();
                        boolean isIPv4 = host_address.indexOf(':')<0;
                        if (useIPv4) {
                            if (isIPv4)
                                return host_address;
                        } else {
                            if (!isIPv4) {
                                int delimiter = host_address.indexOf('%'); // drop ip6 zone suffix
                                return delimiter<0 ? host_address.toUpperCase() : host_address.substring(0, delimiter).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    /**
     * Get the IP of current Wi-Fi connection
     * @return IP as string
     */
    public String getIP(MainActivity main_activity) {
        try {
            WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            String ip = String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
            if (ip.equals("0.0.0.0")) {
                return null;
            }
            return ip;
        } catch (Exception ex) {
            main_activity.printLine(ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    /**
    * HTML encode of UTF8 string i.e. symbols with code more than 127 aren't encoded
    * Use Apache Commons Text StringEscapeUtils if it is possible
    *
    * <pre>
    * escapeHtml("\tIt's time to hack & fun\r<script>alert(\"PWNED\")</script>")
    *    .equals("&#9;It&#39;s time to hack &amp; fun&#13;&lt;script&gt;alert(&quot;PWNED&quot;)&lt;/script&gt;")
    * </pre>
    */
    public static String escapeHtml(String rawHtml) {
        int rawHtmlLength = rawHtml.length();
        // add 30% for additional encodings
        int capacity = (int) (rawHtmlLength * 1.3);
        StringBuilder sb = new StringBuilder(capacity);
        for (int i = 0; i < rawHtmlLength; i++) {
            char ch = rawHtml.charAt(i);
            if (ch == '<') {
                sb.append("&lt;");
            } else if (ch == '>') {
                sb.append("&gt;");
            } else if (ch == '"') {
                sb.append("&quot;");
            } else if (ch == '&') {
                sb.append("&amp;");
            } else if (ch < ' ' || ch == '\'') {
                // non printable ascii symbols escaped as numeric entity
                // single quote ' in html doesn't have &apos; so show it as numeric entity &#39;
                sb.append("&#").append((int)ch).append(';');
            } else {
                // any non ASCII char i.e. upper than 127 is still UTF
                sb.append(ch);
            }
        }
        return sb.toString();
    }

}