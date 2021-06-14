// ES2020 modules

import * as cm from "./common.js"

// region: module scope variables
// this web page can be in different states. A state defines which elements are hidden or visible.
const PageState = {
    Start: 'Start',
    Waiting: 'Waiting',
    SongPlay: 'SongPlay',
    ConnectionLost: 'ConnectionLost',
    Bye: 'Bye'
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
    cm.el("button_reload").addEventListener("click", () => { state_transition_to_start(); });
    // endregion: event listeners

    state_transition_to_start();
}

function connect_to_guitaraoke_server() {
    cm.connect_to_guitaraoke_server();

    globalThis.websocket.onmessage = function(event) {
        let msg = JSON.parse(event.data);
        //console.log(`[message] : ${msg.data}`);
        if (msg.data.startsWith("song: ")) {
            let song_name = msg.data.substring(6);
            state_transition_from_waiting_to_song_load(song_name);
        } else if (msg.data == "play!") {
            state_transition_from_song_load_to_play();
        } else if (msg.data == "stop!") {
            state_transition_from_play_to_waiting();
        } else if (msg.data == "bye!") {
            state_transition_to_bye();
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
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    cm.el("div_bye").hidden = true;
    //cm.el("div_debug").hidden = true;
}

function state_ui_waiting() {
    page_state = PageState.Waiting;
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    cm.el("div_bye").hidden = true;
}

function state_ui_song_load() {
    page_state = PageState.SongLoad;
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    cm.el("div_bye").hidden = true;
    video_video.muted = true;
}

function state_ui_play() {
    page_state = PageState.SongPlay;
    cm.el("div_follower").hidden = false;
    cm.el("div_connection_lost").hidden = true;
    cm.el("div_bye").hidden = true;
    //cm.el("div_debug").hidden = false;
}

function state_ui_connection_lost() {
    page_state = PageState.ConnectionLost;
    cm.el("div_follower").hidden = true;
    cm.el("div_connection_lost").hidden = false;
    cm.el("div_bye").hidden = true;
}

function state_ui_bye() {
    page_state = PageState.Bye;
    cm.el("div_follower").hidden = true;
    cm.el("div_connection_lost").hidden = true;
    cm.el("div_bye").hidden = false;
}
// endregion: state UI transformation

// region: state transition
function state_transition_to_start() {
    state_ui_start();
    connect_to_guitaraoke_server();
}

function state_transition_from_start_to_waiting() {
    state_ui_waiting();
    // video fullScreen iPhone/Safari is always different
    if (video_video.requestFullscreen) {
        video_video.requestFullscreen();
    } else if (video_video.webkitEnterFullScreen) {
        video_video.webkitEnterFullScreen();
    } else if (video_video.mozRequestFullScreen) {
        video_video.mozRequestFullScreen();
    }
}

function state_transition_from_waiting_to_song_load(song_name) {
    state_ui_song_load();
    console.log("song_load: " + song_name);
    cm.song_load(song_name);
}

function state_transition_from_song_load_to_play() {
    state_ui_play();
    console.log("play");
    video_video.play();
}

function state_transition_from_play_to_waiting() {
    state_ui_waiting();
    //cm.exit_full_screen();
    video_video.pause();
    video_video.src = "videos/Welcome_to_guitaraoke - guitaraoke.mp4";
    video_video.load();
}

function state_transition_to_bye() {
    state_ui_bye();
    cm.exit_full_screen();
}
// endregion: state transition