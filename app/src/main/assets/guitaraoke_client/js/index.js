// ES2020 modules

import * as cm from "./common.js"

// ES2020 import the wasm code compiled from rust with wasm-bindgen
// define an alias for calling functions in this module, example: `* as qr`
////import init, * as qr from '../pkg/qrcode53bytes_lib_for_javascript.js';
// default function init() is called without the alias
//await init();

// call to a function in the wasm module using the alias
//let qr_svg = qr.svg_qrcode("123");
//cm.cm.el("div_qrcode").innerHTML = qr_svg;

// region: module scope variables
// this web page can be in different states. A state defines which elements are hidden or visible.
const PageState = {
    Start: 'Start',
    Waiting: 'Waiting',
    SongLoad: 'SongLoad',
    SongPlay: 'SongPlay',
    Bye: 'Bye'
};

let page_state = PageState.Start;
let button_start = cm.el("button_start");
let div_waiting = cm.el("div_waiting");
let video_video = cm.el("video_video");
let div_start = cm.el("div_start");
let div_play_video = cm.el("div_play_video");
let div_song_name = cm.el("div_song_name");
let button_fullscreen = cm.el("button_fullscreen");
let sync_video_request_interval;
// endregion: module scope variables

// region: global variables
// globalThis.websocket
// globalThis.user_name
// globalThis.sync_clock_correction
// endregion: global variables

export function start_script() {


    // region: event listeners
    window.onresize = function() { cm.warn_user_to_change_orientation(); };
    cm.el("button_start").addEventListener("click", () => { state_transition_from_start_to_waiting(); });
    cm.el("button_fullscreen").addEventListener("click", () => { video_video.requestFullscreen(); });
    cm.el("button_slower").addEventListener("click", () => { modify_play_rate(-0.02); });
    cm.el("button_faster").addEventListener("click", () => { modify_play_rate(+0.02); });
    cm.el("button_muted_toggle").addEventListener("click", () => { button_muted_toggle_on_click(); });
    // endregion: event listeners

    cm.warn_user_to_change_orientation();
    cm.start_sync_clock_with_server();
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
        } else if (msg.data.startsWith("sync_video_reply:")) {
            let splitted = msg.data.split(" ");
            // username, only for me
            let user_name = splitted[1];
            if (user_name == globalThis.user_name) {
                // leader currentTime in milliseconds
                let leader_sync_current_millis = parseInt(splitted[2]);
                let leader_sync_clock_with_correction = parseInt(splitted[3]);
                sync_video_reply(leader_sync_current_millis, leader_sync_clock_with_correction);
            }
        }
    };
}

function sync_video_reply(leader_sync_current_millis, leader_sync_clock_with_correction) {
    let follower_current_millis = video_video.currentTime * 1000;
    let follower_sync_clock_with_correction = Date.now() + globalThis.sync_clock_correction;
    let leader_sync_current_millis_corrected = leader_sync_current_millis + (follower_sync_clock_with_correction - parseInt(leader_sync_clock_with_correction));
    let play_diff_millis = parseInt(leader_sync_current_millis_corrected) - follower_current_millis;
    console.log(play_diff_millis);
    if (play_diff_millis < -20) {
        modify_play_rate(-0.02);
        setTimeout(function() { reset_play_rate(); }, 1000);
    } else if (play_diff_millis > 20) {
        modify_play_rate(+0.02);
        setTimeout(function() { reset_play_rate(); }, 1000);
    } else {
        // stop auto-sync when is ok
        clear_sync_video_interval();
    }
}

function basic_auto_sync() {
    // run now and after interval
    sync_video_request_interval = setInterval(function() { send_sync_video_request(); }, 2000);
    cm.el("button_slower").disabled = true;
    cm.el("button_faster").disabled = true;
}

function send_sync_video_request() {
    cm.send_message("sync_video_request!");
}

function clear_sync_video_interval() {
    cm.el("button_slower").disabled = false;
    cm.el("button_faster").disabled = false;
    clearInterval(sync_video_request_interval);
}

function modify_play_rate(how_much) {
    video_video.playbackRate = video_video.playbackRate + how_much;
    set_rate_inner_text();
}

function reset_play_rate() {
    video_video.playbackRate = 1;
    set_rate_inner_text();
}

function set_rate_inner_text() {
    let rate_caption = "speed";
    // if exists this object
    if (sync_video_request_interval) {
        rate_caption = "auto-sync";
    }
    cm.el('span_speed').innerText = '--- ' + rate_caption + ': ' + video_video.playbackRate.toFixed(3) + ' ---';
}

// region: state UI transformation
function state_ui_start() {
    page_state = PageState.Start;
    div_start.hidden = false;
    button_start.hidden = false;
    div_waiting.hidden = true;
    div_play_video.hidden = true;
}

function state_ui_waiting() {
    page_state = PageState.Waiting;
    div_start.hidden = false;
    button_start.hidden = true;
    div_waiting.hidden = false;
    div_play_video.hidden = true;
}

function state_ui_song_load() {
    page_state = PageState.SongLoad;
    div_start.hidden = true;
    div_play_video.hidden = false;
}

function state_ui_play() {
    page_state = PageState.SongPlay;
    button_fullscreen.hidden = false;
}

function state_ui_bye() {
    page_state = PageState.Bye;
}
// endregion: state UI transformation

// region: state transition
function state_transition_to_start() {
    state_ui_start();
}

function state_transition_from_start_to_waiting() {
    state_ui_waiting();
    connect_to_guitaraoke_server();
}

function state_transition_from_waiting_to_song_load(song_name) {
    state_ui_song_load();
    cm.song_load(song_name);
}

function state_transition_from_song_load_to_play() {
    state_ui_play();
    video_video.play();
    // force basic auto-sync after 1 second
    setTimeout(function() { basic_auto_sync(); }, 1000);
}

function state_transition_from_play_to_waiting() {
    state_ui_waiting();
    cm.exit_full_screen();
    video_video.pause();
    // stop auto-sync when is ok
    clear_sync_video_interval();
}

function state_transition_to_bye() {
    state_ui_bye();
    cm.exit_full_screen();
    window.location = "bye.html";
}
// endregion: state transition

function button_muted_toggle_on_click() {
    if (video_video.muted == true) {
        video_video.muted = false;
        cm.el("button_muted_toggle").innerText = "Mute sound";
    } else {
        video_video.muted = true;
        cm.el("button_muted_toggle").innerText = "Unmute sound";
    }
}