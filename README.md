# Guitaraoke Leader for Android

***Play guitar and sing with your friends***  
***The lyrics and chords show in sync with the "Leader" on any smartphone, tablet, computer or smart TV.***  

[![Licence](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/LucianoBestia/new_date_time_units_and_formats/blob/master/LICENSE) 

## Instructions

To play the game, go to <https://bestia.dev/guitaraoke> and follow the short instructions.

![screenshot](https://github.com/LucianoBestia/GuitaraokeLeader/blob/main/app/src/main/assets/guitaraokewebapp/Poster.png?raw=true)

## Guitaraoke

I never liked karaoke. A single "performer" in front of a uninterested crowd.  
But I like to play guitar and optionally sing along. And I found a buddy guitarist and we can play and sing together.  
I even found some more friends that like to sing along (mostly when drunk).  
And now we have a problem. Nobody remembers the lyrics and the chords.  
I am a programmer and every problem looks like it has a software solutions.  
Enter `Guitaraoke`: it is like karaoke but with added guitar chords.  
It is not a new or original idea. A lot of guitarists came to the same solution. But now we have smartphones!  
There are 2 roles in this game. The `Leader` will lead the way for many `Followers`.  

## Guitaraoke videos

First I created some videos. In the video editor `ShotCut` for Win10, I put together the audio of the song and copied the lyrics and chords I found on the internet. I try to show the lyrics and chords a little bit earlier so we can prepare to sing-along and play-along guitar.  
It is great that I can now modify the chords and the lyrics if I like it different.  
There is a lot of Guitaraoke videos already on the internet. Just google it.  
The `Guitaraoke Leader` app can use any mp4 video you can download to your android.  
TODO: I will make a video tutorial how to create Guitaraoke mp4 files. It is easy.  
TODO: put some reference on others Guitaraoke videos.  

## Mobile first

When we have a party often we don't have a big TV for everybody to see the lyrics. But everybody have a smartphone.  
I would like that everybody easily opens the Guitaraoke mp4 and very important that all the smartphones play it in sync.  

## Ideas

My first idea was to have a web server (on the internet somewhere) with the songRs. But the party is sometimes in the woods or mountains without a good or any internet connection. Even in places with a mobile-data internet connection it can cost money to play videos on some cell phone plans.  Let avoid this.  
When we sing/play we are always near to each other. We can make a local wifi network using the smartphone wifi hot-spot. The smartphones can then see each other and the connection is very fast and costs nothing.  
My second idea was to have a web server on my android smartphone. There are some ready made apps in the Google Play Store, but I wanted a web server I can program with some more functionality like having all smartphones in sync. I don't want to make a native android app. I just try to avoid it. You must use the Android Studio, a language I don't know (java or kotlin) and a IDE user interface that is strange to me.  
My third idea was to use Termux a "Linux terminal" for android. Then make a CLI program in Rust (for the web-server), compile it for the target armv7-linux-androideabi. But it was complicated and other users will not like to use it.  
My forth idea was to use webrtc for peer-to-peer communication. But it must have some sort of signaling server. So we are back on the "server on Android" problem.  

## Android app

My final solution is to make a Web + WebSocket server for Android. Exactly what I wanted to avoid. Eh, ironic.  
I don't plan to publish it on Google Play, but I will create an APK for die-hard fans of Guitaraoke Leader.  
The client will be a simple html/css/javascript project for any browser. No installation whatsoever.
The targeted Android version is 11.0 (API level 30). It will probably not work for older androids.

## Based on WebSocketChat

The basics I got from <https://github.com/JCAguilera/WebSocketChat>  
It crates a web server on the port 8080 and a WebSocket server on port 3000.
It uses [NanoHttpd](https://github.com/NanoHttpd/nanohttpd) for the Webserver, and [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) for the WebSocket Server.
It has a very simple layout and it's pretty easy to use.
I will simplify it as much as possible. The server will be pretty stupid. It will just serve files and broadcast messages to all attached clients. The server has no knowledge at all what is going on.  
This is useful for more than just for Guitaraoke. Maybe I will use the same concept for other projects of mine.
For debugging purposes I will print on the server app messages and debugging info.  

## Android Studio

There is no escape to use Android Studio for Android development. I use it on Win10.  
I attached my Lenovo tablet over USB. On the tablet in `Settings`-`About tablet` I clicked 7 times on the `Build number`. That enables the `Developer options`. Then in `Developer Options` I enabled `Stay awake` and `USB debugging`. I needed to try with different USB cables to make it work.  
Finally it shows in Android Studio in the Toolbar in `Running devices` before the `Run` button.  
I use a Samsung A50 Android 11.0 (API level 30).  

## Local network

This server will work only on a local network like `192.168.x.y`.  
The Leader will create a "mobile hotspot" wi-fi network on his smartphone. All other phones will connect to this network.
I will try to create a QR code to make that easy.
To avoid having heavy internet traffic from all the connected phone it is wise to limit the bandwidth of the outside internet.  
This is easily done: change the "Mobile networks - Network mode" to "2G only".  
This is so slow that even if the smartphones start to download a massive update, it will be very little traffic.
Don't forget to return the "Network mode" to LTE, after using the Karaoke Leader.    

## Guitaraoke Follower

The webapp project is inside the android project in  `GuitaraokeLeader\app\src\main\assets\guitaraokewebapp\`.  
It is just a web page for the Guitaraoke Follower. Simple html/css/javascript.  
I can use Visual Studio Code to edit this, because I like it more than the android studio for html/css/javascript.  
This `assets` folder is distributed with the server installation.  

## Icons

In Android Studio right click on `app - src - main - res` and open `New - Image Asset`. This opens the `Asset Studio` where the icons are edited and exported into different files.  

## Android assets and ExternalStorage

Files that are distributed with the server are called assets. They are read-only and accessible with the object AssetManager.  
I want the video files to be downloadable. The `Leader` can download any `mp4` file from the internet. It will be saved in the ExternalStorage.  
I used the DownLoadManager object for that.  
This is still a "private" storage only for this app and it does not need any special permission.  

## Guitaraoke Leader app

Only the `Leader` needs to install the `Guitaraoke Leader` on his android device.  
The super short instructions are here: <https://bestia.dev/guitaraoke>.  
This app is safe: <https://github.com/LucianoBestia/GuitaraokeLeader/releases/tag/v2.1">.

## The Download songs page

After the installation there is only one video file available: `Welcome to Guitaraoke Leader`.  Other songs videos need to be downloaded.  
My guitaraoke mp4 files are listed here: <https://bestia.dev/guitaraoke/>. There are also other similar videos on the internet. If you can download the `mp4` file, you can add it to `Guitaraoke Leader`.  
The songs are saved in the device folder: `/storage/emulated/0/Android/data/dev.bestia.GuitaraokeLeader/files/videos/`.  
You can also just copy your `mp4` files into this folder using other tools.  

## Javascript ES2020

Javascript is a terrible language and I don't like it and I don't use it unless I have to. This project is easy to write in javascript. But wait, it is called ECMA script now? It deserves a terrible name like this. Somebody calls it `modern javascript`. It is in version ES2020. This version is supported by all modern browsers. Still terrible and now even super confusing. All the code you find around on the internet is for an unknown version. Fantastic. So you cannot recognize `bad or good habits`. And there is absolutely no help for the developer from the interpreter/JIT. Bad, bad language.  
Maybe `modules` in 2020 will make it better. Eh, just a tiny bit.  
Separate files for `js` and `html`, that makes sense.  
Inline event handlers are easy to use, but not recommended in `modern javascript`, so I must use `addEventListener()`.  
`let` is better that `var`.  
`globalThis` is better than `var` or `window`.  
If a variable is not declared, I get an error in runtime. Before you must write `use strict`, for modern  modules it is implicit. Good, better than before (silent declaration leading to incorrect execution), but still not enough for developer comfort.  
`<script type="module" >import * as j from './js/index.js';j.start_script();</script>` is better than other ways to import or include scripts. Having an alias for the module exported functions is very precise: `j.send_msg();`.  
I think I will eventually start to use Typescript instead of javascript for projects of any size, even the smallest one.  

## State and state_transition

It is much easier to think about a page with the concept of `states`.  
One page can be in different clearly defined `states`. One state defines slightly different user interface, 
some elements are hidden, others are visible. There is a limited number of states, this is what makes it easy to grasp and understand.  
The transition from one state to the other defines the actions to be done.  

## Sync playback

It is difficult to make the same video on different devices to play in sync. This is my take.  

### Internal clock correction

First I wanted to have the same exact time on all devices. I am surprised how much the devices internal clock can differ. How can they sync anything on the internet or use the GPS? I was sure the device sync the time with some atomic clock on the network. But it looks it is so much more complicated than I though. I cannot use any internet atomic clock because we will sing and play in the mountains without any internet connection.  
Ok, I need to do some rudimentary clock correction. The GuitaraokeLeader will be our clock of reference for all other devices. Every follower requests the time from the web server. I tried with websockets but it was wildly inaccurate. The follower requests 5 times in interval of 1 second and calculates the average. There are 4 points in time to remember:  
a. sent request  
b. received request  
c. sent response  
d. received response  
We can calculate the average time that the packets travel between the client and server:  
`((d-a)-(c-b))/2`  
We than choose the fastest of repetitions as reference.  
Then we calculate the difference of device clocks in milliseconds. This becomes the `globalThis.sync_clock_correction`. We cannot change the internal clock of the device (for security reasons). We will just correct it for our calculations.  

### Video sync

I tried to set the `currentTime` property of the `video` element, but it didn't work. I decided than to change the playback speed. It is called `video.playbackRate`.  
I put buttons on the screen for faster and slower `playbackRate`. But manually adjusting is tedious.  
My basic auto-sync works like this:  
When the video starts to play the `follower` sends a websocket msg to the `leader` for sync_video. The `leader` response contains the `video.currentTime` and the `corrected system clock in milliseconds`. The follower calculates his corrected clock and compares to the clock of the server. There is always some differences because of the network latency. Add this difference to the received video.currentTime. If the local video.currentTime is much bigger than slow down the video for one second. If the server video is behind, than speed up the local video for one second. After 2 seconds from the request the follower sends another request. It uses playbackRate to speed up or slow the video until the difference is small enough. Then the `setInterval` for sync requests is stopped.  
This way we achieved very similar `video.currentTime`. But sometimes it is not perfect. And users must do the fine adjustment manually.  
If all this is too much for the follower, he can just mute the sound. The only mandatory sound comes from the Leader.  

### Capitulation

I give up. Perfectly sync video playback on many devices is not possible. The sound is the problem, we can hear the smallest out-of-sync. The solution is simple. Mute the sound by default.  
The Followers need only to see the lyrics. The sound will come only from the Leader. Probably he will use a loud bluetooth speaker connected to his phone. Done! Full capitulation :-(  

## WebView

Having separate app for the server and open Chrome for the Leader page didn't work out, because the server went to sleep when there was no user interaction. 
I added a WebView in the app and now the Leader page is inside the app. The server text for debugging is now accessible with the 
button `Show log`.  

## TODO

leader page can have the QR-code for wi-fi network password
on my Samsung A50 in "Mobile hotspot" I have the possibility to create a QR code and save it as image.
Then this image can be shown on the Guitaraoke Leader program. Other phones just read it and boom, they are connected. Easy!
It is wise to change the wi-fi password to "guitaraoke" just for use with Guitaraoke leader, 
and then revert it back to the original wi-fi password for normal use.
Also the follower page can show this QR-code so new followers can use other followers phones, not just the Leader phone.

Leader page can have a QR-code for the URL. It can be created inside the Leader program and shown on the Leader smartphone.
Followers read the QR code with their smartphone and boom, they opened the browser on this url.
Also the follower page can show this QR-code so new followers can use other followers phones, not just the Leader phone.

The storage I use now deletes the files after some time. I need a storage that has permanent files.
  
make a video tutorial.
  
download from any URL
