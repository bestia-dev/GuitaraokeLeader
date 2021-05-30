# GuitaraokeServer

After many different approaches to guitaraoke, I decided to make a Web+WebSocket Server for Android.  
I don't plan to publish it on Google Play, but I will create an APK for die-hard fans of Guitaraoke.   
The client will be a simple html/css/javascript project for any browser.

## Based on WebSocketChat

The basics I got from <https://github.com/JCAguilera/WebSocketChat>
It crates a web server on the port 8080 and a WebSocket server on port 3000.
It uses [NanoHttpd](https://github.com/NanoHttpd/nanohttpd) for the Webserver, and [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) for the WebSocket Server.
It has a very simple layout and it's pretty easy to use.
I will simplify it as much as possible. The server will be pretty stupid. It will just serve files and broadcast messages to all attached clients.  
The server has no knowledge at all what is going on.  
That is useful for more than just Guitaraoke. Maybe I will use the same concept for other projects of mine.
For debugging purposes I will leave that the server displays all the messages.  

## Android Studio

There is no escape to use Android Studio for Android development. I use it on Win10.  
I attached my Lenovo tablet over USB. On the tablet in `Settings`-`About tablet` I clicked 7 times on the `Build number`. That enables the `Developer options`. Then in `Developer Options` I enabled `Stay awake` and `USB debugging`. I needed to try with different USB cables to make it work.  
Finally it shows in Android Studio in the Toolbar in `Running devices` before the `Run` button.  
Sometimes I have to run the Server twice to make the WebSocket Server available. I don't know why, but eventually it works.  

## Local network

This server will work only on a local network like 192.168.x.y. My local network (wi-fi) is the hotspot from my smartphone. I have my Lenovo Win10+WSL2 notebook and a lenovo tablet connected.  
It is possible to open the html file on one ip address and use the WebSocket on another ip address. This makes developing the html app easier. There is no need to publish the server to my android device only because the html has changed.  
 
 

