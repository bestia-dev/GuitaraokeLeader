"use strict";

 var sync_clock_send_timestamp;        
 var sync_clock_received_timestamp;
 var sync_clock_reply_timestamp;
 var sync_clock_got_reply_timestamp;
 var sync_clock_counter=0;
 var sync_clock_interval;
 var sync_clock_min_ping_one_way=1000;
 var sync_clock_diff_1;
 var sync_clock_diff_2;
 var sync_clock_correction;

// shortcut for document.getElementById
function el(element_id){
    return document.getElementById(element_id);
}

function warn_user_to_change_orientation(){
    if (window.screen.orientation.type.startsWith("portrait")){
        document.getElementById("div_warning_to_user").innerText ="Please rotate the device to landscape mode!";
        document.getElementById("div_warning_to_user").hidden=false;
    }else{
        document.getElementById("div_warning_to_user").hidden=true;
    }
}

function song_load(song_name) {                                                   
    div_song_name.innerText = song_name;
    video_video.src = "videos/" + song_name + " - guitaraoke.mp4";
    video_video.load();
}

function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function send_message(data) {
    // the message must be json{String username, String timestamp, String data}
    var msg = JSON.stringify({
        "username": globalThis.user_name,
        "timestamp": Date.now(),
        "data": data
    });
    globalThis.websocket.send(msg);
}

function exit_full_screen() {
    if (document.fullscreenElement) {
        document.exitFullscreen()
            .then(() => console.log("Document Exited from Full screen mode"))
            .catch((err) => console.error(err))
    }
}       

function start_sync_clock_with_server(){
    var sync_clock_interval =  setInterval(request_sync_clock,1000);           
}
function request_sync_clock(){
    // stop interval after 5 times
    if (sync_clock_counter>5){
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
                    sync_clock_got_reply_timestamp = Date.now();
                    // the response is 2 timestamp delimited with a space
                    let splited = http_request.responseText.split(" ");
                    sync_clock_received_timestamp = parseInt(splited[0]);
                    sync_clock_reply_timestamp = parseInt( splited[1]);
                    // the fastest "ping"
                    if (sync_clock_min_ping_one_way > (sync_clock_got_reply_timestamp-sync_clock_send_timestamp)/2){
                        sync_clock_min_ping_one_way = (sync_clock_got_reply_timestamp-sync_clock_send_timestamp)/2;
                        
                        sync_clock_diff_1 =  sync_clock_received_timestamp - (sync_clock_send_timestamp + sync_clock_min_ping_one_way);
                        sync_clock_diff_2 = sync_clock_got_reply_timestamp - (sync_clock_reply_timestamp + sync_clock_min_ping_one_way);
                        sync_clock_correction = (sync_clock_diff_1 - sync_clock_diff_2)/2;
                        //console.log(sync_clock_min_ping_one_way+"\n"+sync_clock_diff_1+"\n"+sync_clock_diff_2+"\n"+sync_clock_correction);
                        //console.log(sync_clock_send_timestamp+"\n"+sync_clock_received_timestamp+"\n"+sync_clock_got_reply_timestamp+"\n"+sync_clock_got_reply_timestamp);
                    }
                    
                }
            }
            sync_clock_send_timestamp = Date.now();
            http_request.send();                    
        }
    }
}
