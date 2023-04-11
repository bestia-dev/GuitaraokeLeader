// ES2020 modules

import * as cm from "./common.js"

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
// window.websocket
// window.user_name
// endregion: global variables

export function start_script() {
    console.log("start_script");
    page_state = PageState.SongList;
    // region: event listeners
    // must use event listener for everything. Must avoid inline events in HTML. But they are so handy.
    // instead of click, I use transitionend. It waits for the transition to end.
    // transitionend is fired multiple times for every transitioned css property. I must take in account only one single property. background-color.
    cm.el("button_download_song").addEventListener("transitionend", () => {if(event.propertyName !== 'background-color') return; button_download_song_on_click(); });
    cm.el("button_back_to_list").addEventListener("transitionend", () => {if(event.propertyName !== 'background-color') return; cm.send_message('stop!'); });
    cm.el("button_send_play").addEventListener("transitionend", () => {if(event.propertyName !== 'background-color') return; state_transition_from_song_load_to_song_play(); cm.send_message('play!'); });
    cm.el("button_fullscreen").addEventListener("transitionend", () => {if(event.propertyName !== 'background-color') return; video_video.requestFullscreen(); });
    cm.el("button_send_stop").addEventListener("transitionend", () => {if(event.propertyName !== 'background-color') return; cm.send_message('stop!'); });
    cm.el("button_qrcode").addEventListener("transitionend", () => { if(event.propertyName !== 'background-color') return; button_qrcode_on_click(); });

    // listener to all songs in the list
    for (let x of document.getElementsByClassName("class_song_name")) {
        x.addEventListener("transitionend", (event) => { if(event.propertyName !== 'background-color') return; send_song_url(event); });
    }
    video_video.addEventListener('ended', my_video_ended);
    // endregion: event listeners

    state_transition_to_song_list();
    //connect immediately to server
    connect_to_guitaraoke_server()
}

function connect_to_guitaraoke_server() {
    cm.connect_to_guitaraoke_server();

    window.websocket.onmessage = function(event) {
        let msg = JSON.parse(event.data);
        //console.log(`[message] : ${msg.data}`);
        if (msg.data.startsWith("song: ")) {
            // the next line is executed before send msg
            //let song_url = msg.data.substring(6);
            //state_transition_from_song_list_to_song_load(song_url);
        } else if (msg.data == "play!") {
            // the next line is executed before send msg
            //state_transition_from_song_load_to_song_play();
        } else if (msg.data == "stop!") {
            state_transition_from_song_play_to_song_list();
        } else if (msg.data.startsWith("connections:")) {
            // msg from the server. One connection is the Leader itself. 
            let followers = msg.data.substring(13) - 1;
            cm.el("followers").innerText = followers;
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
    };
}

// region: state UI transformation
function state_ui_song_list() {
    page_state = PageState.SongList;
    cm.el("div_song_list").hidden = false;
    cm.el("div_play_video").hidden = true;
    cm.el("div_qrcode").hidden = true;

    cm.el("button_download_song").hidden = false;
    cm.el("button_back_to_list").hidden = true;
    cm.el("button_send_play").hidden = true;
    cm.el("button_fullscreen").hidden = true;
    cm.el("button_send_stop").hidden = true;
    cm.el("button_qrcode").hidden = false;
}

function state_ui_song_load() {
    page_state = PageState.SongLoad;
    cm.el("div_song_list").hidden = true;
    cm.el("div_play_video").hidden = false;
    cm.el("div_qrcode").hidden = true;

    cm.el("button_download_song").hidden = true;
    cm.el("button_back_to_list").hidden = false;
    cm.el("button_send_play").hidden = false;
    cm.el("button_fullscreen").hidden = true;
    cm.el("button_send_stop").hidden = true;
    cm.el("button_qrcode").hidden = true;
}

function state_ui_song_play() {
    page_state = PageState.SongPlay;
    cm.el("div_song_list").hidden = true;
    cm.el("div_play_video").hidden = false;
    cm.el("div_qrcode").hidden = true;

    cm.el("button_download_song").hidden = true;
    cm.el("button_back_to_list").hidden = true;
    cm.el("button_send_play").hidden = true;
    cm.el("button_fullscreen").hidden = false;
    cm.el("button_send_stop").hidden = false;
    cm.el("button_qrcode").hidden = true;
}

// endregion: state UI transformation

// region: state transition
function state_transition_to_song_list() {
    state_ui_song_list()
}

function state_transition_from_song_list_to_song_load(song_url) {
    state_ui_song_load();
    cm.song_load(song_url);
}

function state_transition_from_song_load_to_song_play() {
    state_ui_song_play();
    video_video.requestFullscreen();
    video_video.play();
}

function state_transition_from_song_play_to_song_list() {
    state_ui_song_list();
    video_video.pause();
    cm.exit_full_screen();
}

// endregion: state transition

function button_download_song_on_click() {
    location = "https://bestia.dev/guitaraoke/songs.html";
}

function send_song_url(event) {
    let song_url = event.currentTarget.getAttribute("data-url");
    console.log("song_url: " + song_url)
    cm.send_message("song: " + song_url);
    state_transition_from_song_list_to_song_load(song_url);
}

function my_video_ended() {

    cm.send_message('stop!');
    cm.exit_full_screen();
}

function button_qrcode_on_click() {
    if (isHidden(cm.el("div_qrcode"))) {
        cm.el("div_qrcode").hidden = false;
    }else{
        cm.el("div_qrcode").hidden = true;
    }
}

function isHidden(el) {
    return (el.offsetParent === null)
}

function sleep(milliseconds) {
  const date = Date.now();
  let currentDate = null;
  do {
    currentDate = Date.now();
  } while (currentDate - date < milliseconds);
}