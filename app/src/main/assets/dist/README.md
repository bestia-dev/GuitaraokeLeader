[comment]: # (lmake_md_to_doc_comments segment start A)

# guitaraoke_client

[comment]: # (lmake_cargo_toml_to_md start)

**play guitaraoke mp4 on multiple phones in sync**  
***[repo](https://github.com/LucianoBestia/guitaraoke_client); version: 2021.522.823  date: 2021-05-22 authors: Luciano Bestia***  

[comment]: # (lmake_cargo_toml_to_md end)

[comment]: # (lmake_lines_of_code start)

[comment]: # (lmake_lines_of_code end)

[![Licence](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/LucianoBestia/guitaraoke_client/blob/master/LICENSE) [![Rust](https://github.com/LucianoBestia/guitaraoke_client/workflows/RustAction/badge.svg)](https://github.com/LucianoBestia/guitaraoke_client/)

## guitaraoke

I never liked karaoke. A single "performer" in front of a non-interested crowd.  
But I like to play guitar and optionally sing along. And I found a buddy guitarist and we can play and sing together.  
I even found some more friends that like to sing along (mostly when drunk).  
And now we have a problem. Nobody remembers the lyrics and the chords.  
I am a programmer and every problem looks like it has a software solutions.  
Enter `guitaraoke`: it is like karaoke but with added guitar chords.  

## guitaraoke videos

First I created the videos. In the video editor `ShotCut` for Win10, I put together the audio of the song and copied the lyrics and chords I found on the internet. I tried to show the lyrics and chords a little bit earlier so we can prepare to sing-along and play-along guitar.  

## only mobile

When we have a party often we don't have a big TV for everybody to see the lyrics. But everybody have a smartphone.  
I would like that everybody easily opens the guitaraoke mp4 and very important that all the smartphones play it in sync.  

## ideas

My first idea was to have a web server (on the internet somewhere) with the songs. But the party is sometimes in the woods or mountains without a good or any internet connection. Even in places with a mobile-data internet connection it can cost money to play videos on some cell phone plans.  Let avoid this.  
When we sing/play we are always near to each other. We can make a local wifi network using the smartphone wifi hot-spot. The smartphones can then see each other and the connection is very fast and costs nothing.  
My second idea was to have a web server on my android smartphone. There are some ready made apps in the Google Play Store, but I wanted a web server I can program with some more functionality like having all smartphones in sync. I don't want to make a native android app. I just try to avoid it. You must use the Android Studio, a language I don't know (java or kotlin) and a user interface that is strange to me.  
My third idea was to use Termux a "Linux terminal" for android. Then make a CLI program in Rust (for the web-server), compile it for the target armv7-linux-androideabi. But it was complicated and other users will not like to use it.  
My forth idea was to use webrtc for peer-to-peer communication. But it must have some sort of signaling server. So we are again on the "server on Android" problem.  

## final solution

My final solution is to make a Web+WebSocket server for Android. Exactly what I wanted to avoid. Eh, ironic.  
The server code is here <https://github.com/LucianoBestia/GuitaraokeServer>. The client code is separate in this project.  
When ready it will be copied to the folder GuitaraokeServer\app\src\main\assets\dist\ for distribution together with the server.  
First I will start with a simple html/css/javascript application.  

## play-flow

1. All the smartphones must be connected to the same local wifi network. Because the streaming will be fast without interruptions.  
2. The leader opens the GuitaraokeServer android app. It shows the ip address and port that everybody must use.  
3. The leader opens the browser with the server local ip address and the leader.html page.  
4. the followers open the browser on the server ip address and index.html page.  
5. The leader see how many followers are connected.  
6. The leader chooses the song. It broadcasts a msg to all connected followers with the name of the song.  
7. The followers load this song from the GuitaraokeServer and send a msg that the song is loaded.  
8. The leader see that the followers have downloaded the song.
9. The leader press Play and it sends a msg to all connected followers to start playing he song.  

## development

I work in Visual Studio Code on Win10. I have most of my projects in WSL2 Debian. So I will have this too. No other reason.  
This will be only static html/css/javascript files.  
In Win10 I can open the HTML file just by double-clicking it. Don't need to have a web server.  
Inside the javascript it will connect to the Guitaraoke WebSocket Server. That must be running on a known ip address+port. In my case I will have it running on my lenovo tablet. When the tablet screen is go to sleep, the server stops working. I modified the settings to `Sleep after 30 minutes of inactivity" and it works fine for me. The ip address of the tablet keeps changing and I need to change it in my browser and in my development index.html and leader.html.

## Problem

The first time I connect to the WebServer no problem. But when I reload the page it does not connect to websocket. Why?  

## TODO

ScrollView+TextView better for debugging instead of list of msg.
for leader
Web server directory listing
mp4 load for client
upload mp4 for leader
