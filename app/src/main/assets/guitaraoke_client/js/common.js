
// shortcut for document.getElementById
function el(element_id){
    return document.getElementById(element_id);
}

function warn_user_to_change_orientation(){
    if (window.screen.orientation.type.startsWith("portrait")){
        document.getElementById("div_rotate").hidden=false;
    }else{
        document.getElementById("div_rotate").hidden=true;
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
        "username": user_name,
        "timestamp": Date.now(),
        "data": data
    });
    websocket.send(msg);
}

function exit_full_screen() {
    if (document.fullscreenElement) {
        document.exitFullscreen()
            .then(() => console.log("Document Exited from Full screen mode"))
            .catch((err) => console.error(err))
    }
}       