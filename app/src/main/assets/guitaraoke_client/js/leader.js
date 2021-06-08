// ES2020

// ES2020 import the wasm code compiled from rust with wasm-bindgen
// define an alias for calling functions in this module, example: `* as qr`
////import init, * as qr from '../pkg/qrcode53bytes_lib_for_javascript.js';
// default function init() is called without the alias
//await init();

// call to a function in the wasm module using the alias
//let qr_svg = qr.svg_qrcode("oiuerhgoiushgoiug  jhg jkgh jkgh jhg kgh ");
//document.getElementById("div_qrcode").innerHTML = qr_svg;

// this web page can be in different states. A state defines which elements are hidden or visible.
const PageState = {
SongList: 'SongList',
SongLoad: 'SongLoad',
SongPlay: 'SongPlay',
Bye: 'Bye'
};
     
var page_state=PageState.SongList;
globalThis.websocket;
var server_client_timestamp_diff=0;
var ping_server_ms=0;
var timestamp_before_request=0;
var video_video = el("video_video");
globalThis.user_name;

// region: this is executed directly after html load
export function start_script(){
    console.log("start_script");
    // region: event listeners
    // must use event listener for everything. Must avoid inline events in HTML. But they are so handy.
    el("button_bye").addEventListener("click", () => {send_message('bye!'); });
    el("back_to_list").addEventListener("click", () => {send_message('stop!'); });
    el("send_play").addEventListener("click", () => {send_message('play!'); });
    el("send_pause").addEventListener("click", () => {send_message('stop!'); });
    el("button_fullscreen").addEventListener("click", () => {video_video.requestFullscreen(); });

    for( let x of document.getElementsByClassName("class_song_name")){
        x.addEventListener("click", send_song_name);
    }

    video_video.addEventListener('ended', my_video_ended);
    window.onresize = function(){ warn_user_to_change_orientation(); };
    // endregion: event listeners

    warn_user_to_change_orientation();
    start_sync_clock_with_server();

    state_transition_to_song_list();        
    //connect immediately to server
    connect_to_guitaraoke_server()
}

function connect_to_guitaraoke_server() {
    globalThis.user_name = getRandomInt(0, 1000000);
    let url = window.location.href;
    console.log(url);
    //TODO: this url is fixed only for developing
    //var ws_url = "ws://192.168.104.232:3000";
    //TODO: this url is needed in runtime
    let ws_url = url.replace("http://","ws://").replace("8080","3000");
    console.log(ws_url);
    globalThis.websocket = new WebSocket(ws_url);
    globalThis.websocket.onopen = function(e) {
        console.log("[open] Connection established");
    };

    globalThis.websocket.onmessage = function(event) {
        let msg = JSON.parse(event.data);
        //console.log(`[message] : ${msg.data}`);
        if (msg.data.startsWith("song: ")) {
            let song_name = msg.data.substring(6);
            state_transition_from_song_list_to_song_load(song_name);
        } else if (msg.data == "play!") {                    
            document.getElementById("div_warning_to_user").hidden=true;
            state_transition_from_song_load_to_song_play();                    
        } else if (msg.data == "stop!") {
            state_transition_from_song_play_to_song_list();                    
        } else if (msg.data == "bye!") {
            state_transition_to_bye();
        } else if (msg.data == "sync_request!") {
            let leader_sync_clock_with_correction = Date.now()+sync_clock_correction;
            send_message("sync_reply: "+msg.username+" "+(video_video.currentTime*1000).toString()+" "+ leader_sync_clock_with_correction);                    
        } else if (msg.data.startsWith("sync_reply:")) {       
            // check: there must only 1 leader in the network
            if (msg.username != globalThis.user_name){
                document.getElementById("div_warning_to_user").innerText ="Error: Only 1 Leader is allowed in the network !";
                document.getElementById("div_warning_to_user").hidden=false;
                send_message('stop!');
            }                                 
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

    globalThis.websocket.onerror = function(error) {
        console.log(`[error] ${error.message}`);
    };
}

// region: state UI transormation
function state_ui_song_list(){
    page_state = PageState.SongList;
    el("div_start").hidden = false;
    el("div_play_video").hidden = true; 
    el("button_fullscreen").hidden = true;
}

function state_ui_song_load(){
    page_state = PageState.SongLoad;
    el("div_start").hidden = true;
    el("div_play_video").hidden = false;
    el("back_to_list").hidden = false;
    el("send_play").hidden = false;
    el("send_pause").hidden = true;
}

function state_ui_song_play(){
    page_state = PageState.SongPlay;
    el("div_start").hidden = true;
    el("div_play_video").hidden = false;
    el("send_play").hidden = true;
    el("send_pause").hidden = false;
    el("back_to_list").hidden = true;
    el("button_fullscreen").hidden = false;  
}

function state_ui_bye(){
    page_state = PageState.Bye;
}
// endregion: state UI transormation

// region: state transition
function state_transition_to_song_list(){
    state_ui_song_list()                          
}

function state_transition_from_song_list_to_song_load(song_name){
    state_ui_song_load();
    song_load(song_name);                    
}

function state_transition_from_song_load_to_song_play(){
    state_ui_song_play();            
    video_video.play();
}

function state_transition_from_song_play_to_song_list(){
    state_ui_song_list();
    video_video.pause();
    exit_full_screen();            
}

function state_transition_to_bye(){
    state_ui_bye();
    exit_full_screen();
    window.location ="bye.html";
}

// endregion: state transition

export function send_song_name(event) {
    let song_name = event.currentTarget.innerText;
    console.log("song_name: " + song_name)
    send_message("song: " + song_name);
}

function my_video_ended() {
    send_message('stop!');
    exit_full_screen();
}

