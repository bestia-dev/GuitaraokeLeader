// ES2020 modules

// region: module scope variables      
let sync_clock_interval;
let sync_clock_counter = 0;
// endregion: module scope variables

// region: global variables
// globalThis.websocket
// globalThis.user_name
// globalThis.sync_clock_correction
// globalThis.sync_clock_sent_request;  
// endregion: global variables

// shortcut for document.getElementById
export function el(element_id) {
    return document.getElementById(element_id);
}

export function debug_write(msg) {
    el("div_debug").innerText = el("div_debug").innerText + "\n" + msg;
}

export function connect_to_guitaraoke_server() {
    globalThis.user_name = getRandomInt(0, 1000000);
    let url = window.location.href;
    console.log(url);
    //TODO: this url is fixed only for developing
    //let ws_url = "ws://192.168.104.232:3000";
    //TODO: this url is needed in runtime
    let ws_url = url.replace("http://", "ws://").replace("8080", "3000");
    console.log(ws_url);
    globalThis.websocket = new WebSocket(ws_url);

    globalThis.websocket.onopen = function(e) {
        console.log("[open] Connection established");
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

export function warn_user_to_change_orientation() {
    if (window.screen.orientation.type.startsWith("portrait")) {
        el("div_warning_to_user").innerText = "Please rotate the device to landscape mode!";
        el("div_warning_to_user").hidden = false;
    } else {
        el("div_warning_to_user").hidden = true;
    }
}

export function song_load(song_name) {
    div_song_name.innerText = song_name;
    video_video.src = "videos/" + song_name + " - guitaraoke.mp4";
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
        "username": globalThis.user_name,
        "timestamp": Date.now(),
        "data": data
    });
    globalThis.websocket.send(msg);
}

export function exit_full_screen() {
    if (document.fullscreenElement) {
        document.exitFullscreen()
            .then(() => console.log("Document Exited from Full screen mode"))
            .catch((err) => console.error(err))
    }
}

export function start_sync_clock_with_server() {
    sync_clock_interval = setInterval(request_sync_clock, 1000);
}

function request_sync_clock() {
    let sync_clock_min_ping_one_way = 1000;
    // stop interval after 5 times
    if (sync_clock_counter > 5) {
        clearInterval(sync_clock_interval);
    } else {
        sync_clock_counter += 1;
        // send request to server, store the sent timestamp
        if (window.XMLHttpRequest) {
            let http_request = new XMLHttpRequest();
            let page_url = "sync_clock.html";
            http_request.open("GET", page_url, true);

            // process the reply
            http_request.onreadystatechange = function() {
                if (http_request.readyState == 4 && http_request.status == 200) {
                    let sync_clock_received_response = Date.now();
                    // the response is 2 timestamp delimited with a space
                    let splitted = http_request.responseText.split(" ");
                    let sync_clock_received_request = parseInt(splitted[0]);
                    let sync_clock_sent_response = parseInt(splitted[1]);
                    //debug_write(sync_clock_received_response - globalThis.sync_clock_sent_request);
                    // the fastest "ping"
                    let new_ping = ((sync_clock_received_response - globalThis.sync_clock_sent_request) - (sync_clock_sent_response - sync_clock_received_request)) / 2;
                    if (sync_clock_min_ping_one_way > new_ping) {
                        sync_clock_min_ping_one_way = new_ping;

                        let sync_clock_diff_1 = sync_clock_received_request - (globalThis.sync_clock_sent_request + sync_clock_min_ping_one_way);
                        let sync_clock_diff_2 = sync_clock_received_response - (sync_clock_sent_response + sync_clock_min_ping_one_way);
                        globalThis.sync_clock_correction = (sync_clock_diff_1 - sync_clock_diff_2) / 2;
                        //debug_write(sync_clock_min_ping_one_way + "\n" + sync_clock_diff_1 + "\n" + sync_clock_diff_2 + "\n" + globalThis.sync_clock_correction);
                    }
                }
            }
            globalThis.sync_clock_sent_request = Date.now();
            http_request.send();
        }
    }
}