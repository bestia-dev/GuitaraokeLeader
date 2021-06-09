# Guitaraoke

***Play guitar and sing with your friends***  
***On any smartphone read the lyrics and chords in sync with the song***  

I never liked karaoke. A single "performer" in front of a non-interested crowd.  
But I like to play guitar and optionally sing along. And I found a buddy guitarist and we can play and sing together.  
I even found some more friends that like to sing along (mostly when drunk).  
And now we have a problem. Nobody remembers the lyrics and the chords.  
I am a programmer and every problem looks like it has a software solutions.  
Enter `Guitaraoke`: it is like karaoke but with added guitar chords.  
The are 2 roles in this game. The `Leader` will lead the way for many `followers`.  

## guitaraoke videos

First I created the videos. In the video editor `ShotCut` for Win10, I put together the audio of the song and copied the lyrics and chords I found on the internet. I tried to show the lyrics and chords a little bit earlier so we can prepare to sing-along and play-along guitar. It is great that I can now modify the chords and the lyrics if I like it different.  
TODO: I will make a tutorial how to create Guitaraoke mp4 files. It is easy.  

## only mobile

When we have a party often we don't have a big TV for everybody to see the lyrics. But everybody have a smartphone.  
I would like that everybody easily opens the guitaraoke mp4 and very important that all the smartphones play it in sync.  

## ideas

My first idea was to have a web server (on the internet somewhere) with the songs. But the party is sometimes in the woods or mountains without a good or any internet connection. Even in places with a mobile-data internet connection it can cost money to play videos on some cell phone plans.  Let avoid this.  
When we sing/play we are always near to each other. We can make a local wifi network using the smartphone wifi hot-spot. The smartphones can then see each other and the connection is very fast and costs nothing.  
My second idea was to have a web server on my android smartphone. There are some ready made apps in the Google Play Store, but I wanted a web server I can program with some more functionality like having all smartphones in sync. I don't want to make a native android app. I just try to avoid it. You must use the Android Studio, a language I don't know (java or kotlin) and a IDE user interface that is strange to me.  
My third idea was to use Termux a "Linux terminal" for android. Then make a CLI program in Rust (for the web-server), compile it for the target armv7-linux-androideabi. But it was complicated and other users will not like to use it.  
My forth idea was to use webrtc for peer-to-peer communication. But it must have some sort of signaling server. So we are back on the "server on Android" problem.  

## GuitaraokeServer

My final solution is to make a Web+WebSocket server for Android. Exactly what I wanted to avoid. Eh, ironic.  
I don't plan to publish it on Google Play, but I will create an APK for die-hard fans of Guitaraoke.  
The client will be a simple html/css/javascript project for any browser. No installation whatsoever.

## Based on WebSocketChat

The basics I got from <https://github.com/JCAguilera/WebSocketChat>  
It crates a web server on the port 8080 and a WebSocket server on port 3000.
It uses [NanoHttpd](https://github.com/NanoHttpd/nanohttpd) for the Webserver, and [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) for the WebSocket Server.
It has a very simple layout and it's pretty easy to use.
I will simplify it as much as possible. The server will be pretty stupid. It will just serve files and broadcast messages to all attached clients.  
The server has no knowledge at all what is going on.  
This is useful for more than just for Guitaraoke. Maybe I will use the same concept for other projects of mine.
For debugging purposes I will print on the server app messages and debugging info.  

## Android Studio

There is no escape to use Android Studio for Android development. I use it on Win10.  
I attached my Lenovo tablet over USB. On the tablet in `Settings`-`About tablet` I clicked 7 times on the `Build number`. That enables the `Developer options`. Then in `Developer Options` I enabled `Stay awake` and `USB debugging`. I needed to try with different USB cables to make it work.  
Finally it shows in Android Studio in the Toolbar in `Running devices` before the `Run` button.  
I use an old Lenovo tablet for my server: Android 6.0 (API level 23).  

## Local network

This server will work only on a local network like `192.168.x.y`. My personal local network wi-fi uses hotspot from my smartphone. But it could be any wi-fi hotspot. I have my Lenovo Win10+WSL2 notebook and a lenovo tablet connected to my wifi local network.  
Just to mention a curious behavior when the smartphones hotspot has no mobile-internet connection, it cannot see the local network. All other connected devices work just fine with the local network wi-fi.  

## Guitaraoke Client

The client project is inside the server project in  `GuitaraokeServer\app\src\main\assets\guitaraoke_client\`.  
I can use VisualStudioCode to edit this, because I like it more than the android studio for html/css/javascript.  
This `assets` folder is distributed with the server installation.  

## icons online

I created the android icons with this online service:  
<https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html#foreground.type=clipart&foreground.clipart=android&foreground.space.trim=1&foreground.space.pad=0.25&foreColor=rgba(96%2C%20125%2C%20139%2C%200)&backColor=rgb(68%2C%20138%2C%20255)&crop=0&backgroundShape=square&effects=none&name=ic_launcher>

## android assets and ExternalStorage

Files that are distributed with the server are called assets. They are read-only and accessible with the the object AssetManager.  
I want the video files to be downloadable. The `Leader` can download any  
`xxx - guitaraoke.mp4`  
file from the internet. It will be saved in the ExternalStorage. I used the DownLoadManager object for that.  

## Leader installs GuitaraokeServer app

Only the `leader` needs to install the GuitaraokeServer on his smartphone.  
He downloads the signed APK from Github : <https://github.com/LucianoBestia/GuitaraokeServer/releases>.  
The smartphone must have enabled `Settings - Security - Unknown Sources`. Read more here:  
<https://www.thegeeksclub.com/how-to-install-third-party-apk-on-android/>  
He starts the app. The server is just a black screen with yellow debugging text. Nothing interesting going on here. It just must be opened because it serves the files to the browsers. When the tablet screen goes to sleep, the server stops working. Modify the settings to `Sleep after 10 minutes of inactivity`.  
On start the app opens the browser on the `Leader page`. If you accidentally close the browser, just restart the app. It will open again on the right page.  

## The Download songs page

I put only some sample songs in the app installation. But they are no fun.  
My guitaraoke.mp4 files are listed here: <https://bestia.dev/guitaraoke/>. I hope eventually to see more Guitaraoke files from other people. That will be so much fun.  
Click on the button `Download new songs`.  
Copy the url of the `xxx - guitaraoke.mp4` file and press `Start download`.  
For example `https://www.dropbox.com/s/hn0r9on24dxkhfh/4%20Non%20Blondes%20-%20What%27s%20Up%20-%20guitaraoke.mp4?dl=1`  
It will download in the background, slowly, but surely.  
If you want to download multiple songs at once, copy the song urls one per line. On my `Guitaraoke song page` you can just copy+paste more lines at once.  
Finally click on the button `Back to Guitaraoke Leader page`.  
The songs are saved in the device folder: `/storage/emulated/0/Android/data/dev.bestia.guitaraokeserver/files/videos/`.  

## Easy Play-flow instructions

1. Only the leader must install the android APK on his phone.  
2. Connect all the smartphones from the leader and followers to the same local wifi network.  
3. The leader opens the GuitaraokeServer app. It will automatically open his browser on the `Leader page`.
4. The leader shows the `Follower page` url (address) to the followers. hey can scan the QR code or just type the numbers. It is short and easy.  
5. Followers must click on `Start user interaction` because javascript need a user `interaction` with the page. Nothing else to do for the follower.  
6. The leader clicks on the song name. It broadcasts a msg to all connected followers. They can all see the name of the song.
7. The leader clicks `Play`. It sends a msg to all connected followers to start playing he song.  
8. The follower page will try some basic auto-sync. If the sound is not perfectly in sync, the follower can adjust the speed of the playback. Or he can simply `Mute sound` to avoid the disturbing delayed sound.  
9. Finally click on `Fullscreen` to see the lyrics and chords and play/sings along.  

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

## state and state_transition

It is much easier to think about a page with the concept of `states`.  
One page can be in different clearly defined `states`. One state defines slightly different user interface, some elements are hidden, others are visible. There is a limited number of states, this is what makes it easy to grasp and understand.  
The transition from one state to the other defines the actions to be done.  

## sync playback

It is difficult to make the same video on different devices to play in sync. This is my take.  

### internal clock correction

First I wanted to have the same exact time on all devices. I am surprised how much the devices internal clock can differ. How can they sync anything on the internet or use the GPS? I was sure the device sync the time with some atomic clock on the network. But it looks it is so much more complicated than I though. I cannot use any internet atomic clock because we will sing and play in the mountains without any internet connection.  
Ok, I need to do some rudimentary clock correction. The GuitaraokeServer will be our clock of reference for all other devices. Every follower requests the time from the web server. I tried with websockets but it was wildly inaccurate. The follower requests 5 times in interval of 1 second and calculates the average. There are 4 points in time to remember:  
a. sent request  
b. received request  
c. sent response  
d. received response  
We can calculate the average time that the packets travel between the client and server:  
`((d-a)-(c-b))/2`  
We than choose the fastest of repetitions as reference.  
Then we calculate the difference of device clocks in milliseconds. This becomes the `globalThis.sync_clock_correction`. We cannot change the internal clock of the device (for security reasons). We will just correct it for our calculations.  

### video sync

I tried to set the `currentTime` property of the `video` element, but it didn't work. I decided than to change the playback speed. It is called `video.playbackRate`.  
I put buttons on the screen for faster and slower `playbackRate`. But manually adjusting is tedious.  
My basic auto-sync works like this:  
When the video starts to play the `follower` sends a websocket msg to the `leader` for sync_video. The `leader` response contains the `video.currentTime` and the `corrected system clock in milliseconds`. The follower calculates his corrected clock and compares to the clock of the server. There is always some differences because of the network latency. Add this difference to the received video.currentTime. If the local video.currentTime is much bigger than slow down the video for one second. If the server video is behind, than speed up the local video for one second. After 2 seconds from the request the follower sends another request. It uses playbackRate to speed up or slow the video until the difference is small enough. Then the `setInterval` for sync requests is stopped.  
This way we achieved very similar `video.currentTime`. But sometimes it is not perfect. And users must do the fine adjustment manually.  
If all this is too much for the follower, he can just mute the sound. The only mandatory sound comes from the Leader.  

## TODO

ES2020 and modules
leader page can create QRcode with Follower page  
make a video tutorial  
Playlist: songs and play order  
