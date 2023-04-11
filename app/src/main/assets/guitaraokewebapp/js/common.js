// ES2020 modules

// region: module scope variables      

// endregion: module scope variables

// region: global variables
// use window  instead of globalThis (because of Safari)
// window.websocket
// window.user_name
// endregion: global variables

// shortcut for document.getElementById
export function el(element_id) {
    return document.getElementById(element_id);
}

export function debug_write(msg) {
    el("div_debug").innerText = el("div_debug").innerText + "\n" + msg;
}

export function connect_to_guitaraoke_server() {
    window.user_name = getRandomInt(0, 1000000);
    let url = window.location.href;
    console.log(url);
    //TODO: this url is fixed only for developing
    //let ws_url = "ws://192.168.104.232:3000";
    //TODO: this url is needed in runtime
    let ws_url = url.replace("http://", "ws://").replace("8080", "3000");
    console.log(ws_url);
    window.websocket = new WebSocket(ws_url);

    window.websocket.onopen = function(e) {
        console.log("[open] Connection established");
    };

    window.websocket.onerror = function(error) {
        console.log(`[error] ${error.message}`);
    };
}

export function song_load(song_url) {
    video_video.src = song_url;
    video_video.load();
}

export function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function send_message(data) {
    // the message must be json{String username, String timestamp, String data}
    let msg = JSON.stringify({
        "username": window.user_name,
        "timestamp": Date.now(),
        "data": data
    });
    window.websocket.send(msg);
}

export function exit_full_screen() {
    if (document.fullscreenElement) {
        document.exitFullscreen()
            .then(() => console.log("Document Exited from Full screen mode"))
            .catch((err) => console.error(err))
    }
}
