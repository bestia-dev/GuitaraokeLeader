// ES2020 modules

import * as cm from "./common.js"

// ES2020 import the wasm code compiled from rust with wasm-bindgen
// define an alias for calling functions in this module, example: `* as qr`
////import init, * as qr from '../pkg/qrcode53bytes_lib_for_javascript.js';
// default function init() is called without the alias
//await init();

// call to a function in the wasm module using the alias
//let qr_svg = qr.svg_qrcode("123");
//cm.el("div_qrcode").innerHTML = qr_svg;

// region: module scope variables
// this web page can be in different states. A state defines which elements are hidden or visible.
const PageState = {
    SongList: 'SongList',
    SongLoad: 'SongLoad',
    SongPlay: 'SongPlay',
};

let page_state = PageState.Start;
let video_video = cm.el("video_video");
// endregion: module scope variables

// region: global variables
// globalThis.websocket
// globalThis.user_name
// endregion: global variables

export function start_script() {
    console.log("start_script");
    page_state = PageState.SongList;
    // region: event listeners
    // must use event listener for everything. Must avoid inline events in HTML. But they are so handy.
    cm.el("button_download_song").addEventListener("click", () => { button_download_song_on_click(); });
    cm.el("button_back_to_list").addEventListener("click", () => { cm.send_message('stop!'); });
    cm.el("button_send_play").addEventListener("click", () => { cm.send_message('play!'); });
    cm.el("button_fullscreen").addEventListener("click", () => { video_video.requestFullscreen(); });
    cm.el("button_send_stop").addEventListener("click", () => { cm.send_message('stop!'); });
    // listener to all songs
    for (let x of document.getElementsByClassName("class_song_name")) {
        x.addEventListener("click", send_song_name);
    }
    video_video.addEventListener('ended', my_video_ended);
    // endregion: event listeners

    state_transition_to_song_list();
    //connect immediately to server
    connect_to_guitaraoke_server()
}

function connect_to_guitaraoke_server() {
    cm.connect_to_guitaraoke_server();

    globalThis.websocket.onmessage = function(event) {
        let msg = JSON.parse(event.data);
        //console.log(`[message] : ${msg.data}`);
        if (msg.data.startsWith("song: ")) {
            let song_name = msg.data.substring(6);
            state_transition_from_song_list_to_song_load(song_name);
        } else if (msg.data == "play!") {
            state_transition_from_song_load_to_song_play();
        } else if (msg.data == "stop!") {
            state_transition_from_song_play_to_song_list();
        } else if (msg.data.startsWith("connections:")) {
            // msg from the server. One connection is the Leader itself. 
            let followers = msg.data.substring(13) - 1;
            cm.el("followers").innerText = followers;
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
    };
}

// region: state UI transformation
function state_ui_song_list() {
    page_state = PageState.SongList;
    cm.el("div_start").hidden = false;
    cm.el("div_play_video").hidden = true;

    cm.el("button_download_song").hidden = false;
    cm.el("button_back_to_list").hidden = true;
    cm.el("button_send_play").hidden = true;
    cm.el("button_fullscreen").hidden = true;
    cm.el("button_send_stop").hidden = true;
}

function state_ui_song_load() {
    page_state = PageState.SongLoad;
    cm.el("div_start").hidden = true;
    cm.el("div_play_video").hidden = false;

    cm.el("button_download_song").hidden = true;
    cm.el("button_back_to_list").hidden = false;
    cm.el("button_send_play").hidden = false;
    cm.el("button_fullscreen").hidden = true;
    cm.el("button_send_stop").hidden = true;
}

function state_ui_song_play() {
    page_state = PageState.SongPlay;
    cm.el("div_start").hidden = true;
    cm.el("div_play_video").hidden = false;

    cm.el("button_download_song").hidden = true;
    cm.el("button_back_to_list").hidden = true;
    cm.el("button_send_play").hidden = true;
    cm.el("button_fullscreen").hidden = false;
    cm.el("button_send_stop").hidden = false;
}

// endregion: state UI transformation

// region: state transition
function state_transition_to_song_list() {
    state_ui_song_list()
}

function state_transition_from_song_list_to_song_load(song_name) {
    state_ui_song_load();
    cm.song_load(song_name);
}

function state_transition_from_song_load_to_song_play() {
    state_ui_song_play();
    // the leader will be late a little bit, so the followers can prepare
    setTimeout(video_video.play(), 500);
}

function state_transition_from_song_play_to_song_list() {
    state_ui_song_list();
    video_video.pause();
    cm.exit_full_screen();
}

// endregion: state transition

function button_download_song_on_click() {
    // TODO: allow multiple sources
    location = "https://bestia.dev/guitaraoke/songs.html";
}

function send_song_name(event) {
    let song_name = event.currentTarget.innerText;
    console.log("song_name: " + song_name)
    cm.send_message("song: " + song_name);
}

function my_video_ended() {
    cm.send_message('stop!');
    cm.exit_full_screen();
}