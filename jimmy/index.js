const board = new Board();

let socket = new WebSocket("ws://localhost:8080");

socket.onopen = function(e) {
    console.log('connection opened');
    join();
};

socket.onmessage = function(event) {
    console.log(event.data);
    const msg = JSON.parse(event.data);

    switch (msg.type) {
      case 'game-init':
        handleInit(msg);
        break;
      case 'prompt-roll':
        handleRollPrompt();
        break;
      case 'notify-roll':
        updateRoll(msg);
        break;
      case 'prompt-move':
        selectMove(msg.possibleMoves);
        break;
      case 'notify-move':
        board.displayMove(msg.move);
        break;
      case 'notify-end':
        displayGameOver(msg);
        break;
      default:
        break;
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
  document.getElementById("player-info").classList.add(playingColor);
  playingColor = msg.playingColor;
  board.initColumns(msg.board);
  board.draw();
}

function join() {
  const playerNames = ['Annie', "Anette", "Julie", "Mark", "Novak"];
  const selectedPlayerNameIndex = Math.floor(Math.random() * playerNames.length);
  const selectedPlayerName = playerNames[selectedPlayerNameIndex];

  document.getElementById("player-info").innerText = selectedPlayerName;

  sendJoinMessage(selectedPlayerName, true);
}

function displayGameOver(msg) {
  alert(`You ${playingColor === msg.winner ? 'won' : 'lost'} the game!`);
}

function selectMove(possibleMoves) {
  board.getPlayerMove(possibleMoves).then(selectedMove => {
    const msg = {
      "type": "select-move",
      playingColor,
      selectedMove
    };

    socket.send(JSON.stringify(msg));
  });
}

function updateRoll(msg) {
  document.getElementById("roll-result").innerText = `${msg.dice1} ${msg.dice2} ${msg.playingColor}`;
}

const diceButton = document.getElementById('roll');
diceButton.disabled = true;

function handleRollPrompt() {
  diceButton.disabled = false;
}

diceButton.addEventListener('click', () => {
  console.log("sending roll request")
  socket.send(`{"type": "roll", "playingColor": "${playingColor}"}`);
  diceButton.disabled = true;
});
