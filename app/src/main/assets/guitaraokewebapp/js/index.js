// ES2020 modules

import * as cm from "./common.js"

// region: module scope variables
// this web page can be in different states. A state defines which elements are hidden or visible.
const PageState = {
    Start: 'Start',
    Waiting: 'Waiting',
    SongPlay: 'SongPlay',
    ConnectionLost: 'ConnectionLost'
};

let page_state = PageState.Start;
let video_video = cm.el("video_video");
// endregion: module scope variables

// region: global variables
var isSafari = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
// window.websocket
// window.user_name
// endregion: global variables

export function start_script() {
    // region: event listeners
    // must use event listener for everything. Must avoid inline events in HTML. But they are so handy.
    // instead of click, I use transitionend. It waits for the transition to end.
    // transitionend is fired multiple times for every transitioned css property. I must take in account only one single property. background-color.
    if (isSafari) {
        cm.el("button_reload").addEventListener("click", () => { state_ui_start(); });
        cm.el("button_full_screen").addEventListener("click", () => { button_fullscreen_on_click(); });
        cm.el("button_qrcode").addEventListener("click", () => { button_qrcode_on_click(); });
    }
    else{
        cm.el("button_reload").addEventListener("transitionend", () => { if(event.propertyName !== 'background-color') return; state_ui_start(); });
        cm.el("button_full_screen").addEventListener("transitionend", () => { if(event.propertyName !== 'background-color') return; button_fullscreen_on_click(); });
        cm.el("button_qrcode").addEventListener("transitionend", () => { if(event.propertyName !== 'background-color') return; button_qrcode_on_click(); });
    }
    // endregion: event listeners

    state_ui_start();
}

function connect_to_guitaraoke_server() {
    cm.connect_to_guitaraoke_server();

    window.websocket.onmessage = function(event) {
        let msg = JSON.parse(event.data);
        //console.log(`[message] : ${msg.data}`);
        if (msg.data.startsWith("song: ")) {
            console.log(msg.data);
            let song_url = msg.data.substring(6);
            state_transition_from_waiting_to_song_load(song_url);
        } else if (msg.data == "play!") {
            state_transition_from_song_load_to_play();
        } else if (msg.data == "stop!") {
            video_video.autoplay=true;
            state_ui_waiting();
        }
    };

    window.websocket.onclose = function(event) {
        if (event.wasClean) {
            console.log(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
        } else {
            // e.g. server process killed or network down
            // event.code is usually 1006 in this case
            console.log('[close] Connection died');
        }
        state_ui_connection_lost();
    };
}

// region: state UI transformation
function state_ui_start() {
    page_state = PageState.Start;
    video_video.autoplay=true;
    connect_to_guitaraoke_server();
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    cm.el("button_qrcode").hidden = false;
    cm.el("div_qrcode").hidden = true;
}

function state_ui_waiting() {
    page_state = PageState.Waiting;
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    cm.el("button_qrcode").hidden = false;
    cm.el("div_qrcode").hidden = true;
    cm.song_load("videos/Welcome to Guitaraoke Follower.mp4");
}

function state_ui_song_load() {
    page_state = PageState.SongLoad;
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    cm.el("button_qrcode").hidden = false;
    cm.el("div_qrcode").hidden = true;
}

function state_ui_play() {
    page_state = PageState.SongPlay;
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    cm.el("button_qrcode").hidden = false;
    cm.el("div_qrcode").hidden = true;
}

function state_ui_connection_lost() {
    page_state = PageState.ConnectionLost;
    cm.exit_full_screen();
    cm.el("div_follower").hidden = true;
    cm.el("div_connection_lost").hidden = false;
    cm.el("button_qrcode").hidden = false;
    cm.el("div_qrcode").hidden = true;
}

// endregion: state UI transformation


// region: state transition

function state_transition_from_waiting_to_song_load(song_url) {
    video_video.autoplay=false;
    state_ui_song_load();
    console.log("song_load: " + song_url);
    cm.song_load(song_url);
    video_video.poster = "Poster.jpg";
}

function state_transition_from_song_load_to_play() {
    state_ui_play();
    video_video.play();
}

// endregion: state transition

// click on video to fullscreen. It need a user gesture.
function button_fullscreen_on_click() {
video_video.play();
    // TODO: FullScreen on iPhone does not work
     if (video_video.requestFullscreen) {
        video_video.requestFullscreen();
    } else if (video_video.webkitRequestFullscreen) {
      video_video.webkitRequestFullscreen();
  }
  video_video.play();
}

function button_qrcode_on_click() {
    console.log("button_qrcode_on_click");
    if (isHidden(cm.el("div_qrcode"))) {
        cm.el("div_qrcode").hidden = false;
    }else{
        cm.el("div_qrcode").hidden = true;
    }
}

function isHidden(el) {
    return (el.offsetParent === null)
}