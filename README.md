<!-- markdownlint-disable MD041 -->
[//]: # (auto_md_to_doc_comments segment start A)

# GuitaraokeLeader for Android

**Play guitar and sing with your friends**  
**The lyrics and chords show in sync with the "Leader" on any smartphone, tablet, computer or smart TV.**  
***version: 3.4 date: 2022-06-21 author: [bestia.dev](https://bestia.dev) repository: [GitHub](https://github.com/bestia-dev/GuitaraokeLeader)***  

 ![maintained](https://img.shields.io/badge/maintained-green)
 ![ready_for_use](https://img.shields.io/badge/ready_for_use-green)
 [![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/bestia-dev/GuitaraokeLeader/blob/master/LICENSE)
 ![GuitaraokeLeader](https://bestia.dev/webpage_hit_counter/get_svg_image/136193070.svg)

Hashtags: #java #android #server #tutorial  
My projects on Github are more like a tutorial than a finished product: [bestia-dev tutorials](https://github.com/bestia-dev/tutorials_rust_wasm).

## Instructions

To play the game, go to <https://bestia.dev/guitaraoke> and follow the instructions.

![screenshot](https://github.com/bestia-dev/GuitaraokeLeader/raw/main/app/src/main/assets/guitaraokewebapp/Poster.png)

## Guitaraoke

I never liked karaoke. A single "performer" in front of an uninterested crowd.  
But I like to play guitar and optionally sing along. And I found a buddy guitarist and we can play and sing together.  
I even found some more friends that like to sing along (mostly when drunk).  
And now we have a problem. Nobody remembers the lyrics and the chords.  
I am a programmer and every problem looks like it has a software solutions.  
Enter `GuitaraokeLeader`: it is like karaoke but with added guitar chords.  
It is not a new or original idea. A lot of guitarists came to the same solution.  
But now we have smartphones! There are 2 roles in this game. One `Leader` will lead many `Followers`.  

## Guitaraoke videos

First I created some videos. In the video editor `ShotCut` for Win10, I put together the audio of the song and copied the lyrics and chords I found on the internet. I try to show the lyrics and chords a little bit earlier so we can prepare to sing-along and play-along guitar.  
It is great that I can later modify the chords and the lyrics if I like it different.  
There is a lot of Guitaraoke videos already on the internet. Just google it.  
The `GuitaraokeLeader` app can use any mp4 video you can download to your android.  
TODO: I will make a video tutorial how to create Guitaraoke mp4 files. It is easy.  

## Mobile first

When we have a party often we don't have a big TV for everybody to see the lyrics. But everybody have a smartphone. Everybody can easily play Guitaraoke videos in sync with the Leader.  

## Android app

After different ideas, my final solution is to make a Web + WebSocket server app for Android.  
I don't plan to publish it on Google Play, but I will create an APK and release it on Github. It is easy to install apk from unknown source on Android.  
The client (for the follower) will be a simple html/css/javascript project for any browser. No installation whatsoever.
The targeted Android version is 11.0 (API level 30). It will probably not work for older androids.

## Based on WebSocketChat

The basics I got from <https://github.com/JCAguilera/WebSocketChat>  
It crates a web server on the port 8080 and a WebSocket server on port 3000.
It uses [NanoHttpd](https://github.com/NanoHttpd/nanohttpd) for the Webserver, and [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) for the WebSocket Server.
It has a very simple layout and it's pretty easy to use.
I will simplify it as much as possible. The server will be pretty stupid. It will just serve files and broadcast messages to all attached clients. The server has no knowledge at all what is going on.  
This is useful for more than just for my project GuitaraokeLeader. Maybe I will use the same concept for other projects of mine. 
For debugging purposes I will print on the server app messages and debugging info.  

## Android Studio

There is no escape from using Android Studio for Android development. I use it on Win10.  
I attached my Samsung A50 smartphone with a USB-c cable. On the phone in `Settings-About phone` I clicked 7 times on the Build number. That enables the `Developer options`. Then in `Developer Options` I enabled `Stay awake` and `USB debugging`.  
Finally it shows in Android Studio in the Toolbar in `Running devices` before the `Run` button.  
The version is Android 11.0 (API level 30).  

## Local network

Only the Leader have to work a little to prepare to use this application.  
This app will work only on a local network like `192.168.x.y`.
The Leader will create a `mobile hotspot` wi-fi network on his smartphone. All other phones will connect to this network.
To limit the internet traffic, change the `Mobile networks - Network mode` to `2G only`. After using GuitaraokeLeader, return the `Network mode` to `LTE`.  
Most smartphone can read a QRCode to easily connect to the wi-fi.  

## Guitaraoke Follower

The priject for the follower webapp is inside the android project in  `GuitaraokeLeader\app\src\main\assets\guitaraokewebapp\`.  
It is just a web page for the Guitaraoke Follower. Simple `html/css/javascript`.  
This `assets` folder is distributed with the server installation.  

## Icons

In Android Studio right click on `app - src - main - res` and open `New - Image Asset`. This opens the `Asset Studio` where the icons are edited and exported into different files.  

## Android assets and ExternalStorage

Files that are distributed with the server are called `assets`. They are read-only and accessible with the object `AssetManager`.  
The guitaraoke files are just videos in mp4 format. You can choose the folder where the app will read these files. Recommended location is `Music/Guitaraoke/Rock/` or `Music/Guitraoke/Romantic`. You can have more folders for different kind of music. You have to create these folders manually when you run the app for the first time and Allow access. You can copy the mp4 files using the android file manager.  
The songs I personally prepared are downloadable. Click on the button `Download songs` and then click on a song from the list. It will be downloaded in the background.  

## GuitaraokeLeader app

Only the `Leader` needs to install the `GuitaraokeLeader` APK to his android smartphone.  
The followers will just use their internet browser.  
All the instructions are here: <https://bestia.dev/guitaraoke>.  

## The Download songs page

After the installation there is only one video file available: `Welcome to Guitaraoke Leader`.  
Other songs videos can be downloaded from my web page when you click on `Download song`.  
There are also find similar videos on the internet. Download them and then just copy the `mp4` files into your Guitaraoke folder using the android file manager.  

## Javascript ES2020

Javascript is a terrible language and I don't like it and I don't use it unless I have to. But this project is too easy and I will write in javascript. But wait, it is called ECMA script now? It deserves a terrible name like this. Somebody calls it `modern javascript`. It is in version ES2020. This version is supported by all modern browsers. Still terrible and now even super confusing. All the code you find around on the internet is for an unknown version. Fantastic. So you cannot recognize `bad or good habits`. And there is absolutely no help for the developer from the interpreter/JIT. Bad, bad language.  
Maybe `modules` in 2020 will make it better. Eh, just a tiny bit.  
Separate files for `js` and `html`, that makes sense.  
Inline event handlers are easy to use, but not recommended in `modern javascript`, so I must use `addEventListener()`.  
`let` is better that `var`.  
`globalThis` is better than `var` or `window`, but iPhone Safari does not use it.  
If a variable is not declared, I get an error in runtime. Before you must write `use strict`, for modern  modules it is implicit. Good, better than before (silent declaration leading to incorrect execution), but still not enough for developer comfort.  
`<script type="module" >import * as j from './js/index.js';j.start_script();</script>` is better than other ways to import or include scripts. Having an alias for the module exported functions is very precise: `j.send_msg();`.  
I think I will eventually start to use Typescript instead of javascript for projects of any size, even the smallest one.  

## State and state_transition

It is much easier to think about a page with the concept of `states`.  
One page can be in different clearly defined `states`. One state defines slightly different user interface, some elements are hidden, others are visible. There is a limited number of states, this is what makes it easy to grasp and understand.  
The transition from one state to the other defines the actions to be done.  

## Sync playback

I tried really hard to make the same video play in super-sync on more devices simultaneously.  
I gave up.  
Perfectly sync video playback on many devices is not possible. The sound is the problem, we can hear the smallest out-of-sync. The solution is simple. Mute the sound by default to all followers. The Followers need only to see the lyrics. The sloppy sync is just enough for the lyrics.  
The sound will come only from the Leader. Probably he will use a loud bluetooth speaker connected to his phone.  
Done! Quick and dirty!

## WebView

Having separate app for the server and open Chrome for the Leader page didn't work on the same smartphone, because the server went to sleep when there was no user interaction.  
I added a WebView in the app and now the Leader page is inside the app. The server text for debugging is now accessible with the button `Debug`.  

## APK build

In Android Studio `Build-Generate Signed APK`, use the `KeyStore` in `C:\Users\xxx\AndroidStudioProjects\AndroidKeyStore.jks` with the password you saved somewhere to not forget it.

## Safari on iPhone

Safari on iPhone is a special beast. If everything works fine on Chrome, Firefox, Android,... it 
means nothing to Apple. Safari is the king and it has always something going on outside of the 
standard. For example it does not know about `globalThis`. I must use the old `window` object.

Then the video play(). What a nightmare. Every year something different. Documentation sub-zero. No 
other way to test how it works than having the physical device itself. A true catastrophe.
Finally I found a blog among thousands of people having problems with video on iPhone that 
addresses the `range request` problem. iPhone talks only to servers that can return video in 
`ranges`. 
Not all the servers can do that. Nginx can. I use java nanoHTTP and need to code it on my own.
I hope to find something here:
<https://blog.logrocket.com/streaming-video-in-safari/#:~:text=The%20status%20code%20varies
%20depending,success%20status%20code%20of%20200.>
<https://stackoverflow.com/questions/19359304/how-to-serve-a-file-on-sdcard-using-nanohttpd
-inside-android>

To test it I can use this curl command:

```bash
curl --silent -v --range 20-40 http://192.168.18.251:8080/videos/Welcome to Guitaraoke Leader.mp4
```
This returns 3497681. It does not understand ranges.
This nginx server does understand:

```bash
 curl --silent -v --range 20-40 https://bestia.dev/guitaraoke/videos/Welcome to Guitaraoke Leader.mp4
```

It return 21. Correct answer.

## TODO

make a video tutorial.
iphone little problems

## Open-source and free as a beer

My open-source projects are free as a beer (MIT license).  
I just love programming.  
But I need also to drink. If you find my projects and tutorials helpful, please buy me a beer by donating to my [PayPal](https://paypal.me/LucianoBestia).  
You know the price of a beer in your local bar ;-)  
So I can drink a free beer for your health :-)  
[Na zdravje!](https://translate.google.com/?hl=en&sl=sl&tl=en&text=Na%20zdravje&op=translate) [Alla salute!](https://dictionary.cambridge.org/dictionary/italian-english/alla-salute) [Prost!](https://dictionary.cambridge.org/dictionary/german-english/prost) [Nazdravlje!](https://matadornetwork.com/nights/how-to-say-cheers-in-50-languages/) 🍻

[//bestia.dev](https://bestia.dev)  
[//github.com/bestia-dev](https://github.com/bestia-dev)  
[//bestiadev.substack.com](https://bestiadev.substack.com)  
[//youtube.com/@bestia-dev-tutorials](https://youtube.com/@bestia-dev-tutorials)  

[//]: # (auto_md_to_doc_comments segment end A)
