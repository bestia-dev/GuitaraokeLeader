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
// globalThis.websocket
// globalThis.user_name
// endregion: global variables

export function start_script() {
    // region: event listeners    
    cm.el("button_reload").addEventListener("click", () => { state_ui_start(); });
    cm.el("button_full_screen").addEventListener("click", () => { button_fullscreen_on_click(); });
    // endregion: event listeners

    state_ui_start();
}

function connect_to_guitaraoke_server() {
    cm.connect_to_guitaraoke_server();

    globalThis.websocket.onmessage = function(event) {
        let msg = JSON.parse(event.data);
        //console.log(`[message] : ${msg.data}`);
        if (msg.data.startsWith("song: ")) {
            console.log(msg.data);
            let song_url = msg.data.substring(6);
            state_transition_from_waiting_to_song_load(song_url);
        } else if (msg.data == "play!") {
            state_transition_from_song_load_to_play();
        } else if (msg.data == "stop!") {
            state_ui_waiting();
        }
    };

    globalThis.websocket.onclose = function(event) {
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
    connect_to_guitaraoke_server();
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    //cm.el("div_debug").hidden = true;
}

function state_ui_waiting() {
    page_state = PageState.Waiting;
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    cm.song_load("videos/Welcome to Guitaraoke Leader.mp4");
}

function state_ui_song_load() {
    page_state = PageState.SongLoad;
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
}

function state_ui_play() {
    page_state = PageState.SongPlay;
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    //cm.el("div_debug").hidden = false;
}

function state_ui_connection_lost() {
    page_state = PageState.ConnectionLost;
    cm.exit_full_screen();
    cm.el("div_follower").hidden = true;
    cm.el("div_connection_lost").hidden = false;
}

// endregion: state UI transformation

// region: state transition

function state_transition_from_waiting_to_song_load(song_url) {
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
    console.log("button_fullscreen_on_click");
    if (video_video.requestFullscreen) {
        video_video.requestFullscreen();
    } else if (video_video.webkitEnterFullScreen) {
        video_video.webkitEnterFullScreen();
    } else if (video_video.mozRequestFullScreen) {
        video_video.mozRequestFullScreen();
    }
}
