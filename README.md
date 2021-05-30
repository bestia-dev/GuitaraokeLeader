# GuitaraokeServer

After many different approaches to guitaraoke, I decided to make a WebSocket server on Android.  
The basics I got from <https://github.com/JCAguilera/WebSocketChat>  
I will change just a little bit from the original. First I will remove the functionality for the server to send messages. I don't need that.  
For me is enough that WebSocket broadcast the same message to everybody in the room (Web Socket). Than the client part will decide what to do or not to do.
For debugging purposes I will leave that the server displays all the messages.  
Second: I will change the index.html to my code for Guitaraoke. That has nothing to do wil Android programming, so I hope it will be simple.  

## Android Studio

There is no escape to use Android Studio for Android development. I use it on Win10.  
I attached my Lenovo tablet over USB. On the tablet in `Settings`-`About tablet` I clicked 7 times on the `Build number`. That enables the `Developer options`. Then in `Developer Options` I enabled `Stay awake` and `USB debugging`. I needed to try with different USB cables to make it work.  
Finally it shows in Android Studio in the Toolbar in `Running devices` before the `Run` button.  


# Based on WebSocketChat

Web and Websocket Server example for Android: <https://github.com/JCAguilera/WebSocketChat>. Tested on Android 8.1 (API 27).
Allows you to open a web server to serve an Angular 6 app. Then, opens a Websocket server to communicate with the app in real time through an easy-to-use Chat App.

## Server

It uses [NanoHttpd](https://github.com/NanoHttpd/nanohttpd) for the Webserver, and [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) for the WebSocket Server.
It has a very simple layout and it's pretty easy to use. Just write your username and then start the server.

## Client

The client app is made with Angular 6 and Angular Material. It's stored on the assets folder in the android app.
It's also really easy to use, only username and login. That's it.
