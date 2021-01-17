let socket = new WebSocket("ws://localhost:8080");

socket.onopen = function(e) {
    console.log('connection opened');
    join();
};

socket.onmessage = function(event) {
    console.log(event);
    const msg = JSON.parse(event.data);
    if (msg.type === 'game-init') {
      handleInit(msg);
    }
};

socket.onclose = function(event) {
  if (event.wasClean) {
    console.log("clean close");
  } else {
    console.log('messy close');
  }
};

socket.onerror = function(error) {
    console.error(error);
};

function sendJoinMessage(name, ready) {
  const msg = {
    type: "join",
    playerName: name,
    ready
  };

  socket.send(JSON.stringify(msg));
}

let playingColor;

function handleInit(msg) {
  playingColor = msg.playingColor;
  Object.entries(msg.board.black).forEach(entry => {
    const element = document.getElementById(entry[0]);
    element.innerText = entry[1];
    element.classList.add("black");
  });

  Object.entries(msg.board.white).forEach(entry => {
    const element = document.getElementById(entry[0]);
    element.innerText = entry[1];
    element.classList.add("white");
  });
}

function join() {
  const playerNames = ['Annie', "Anette", "Julie", "Mark", "Novak"];
  const selectedPlayerNameIndex = Math.floor(Math.random() * playerNames.length);
  const selectedPlayerName = playerNames[selectedPlayerNameIndex];

  sendJoinMessage(selectedPlayerName, true);
}
