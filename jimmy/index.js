const board = new Board();


const playerName = getPlayerName();
document.getElementById("player-info").innerText = playerName;
function getPlayerName() {
  const defaultPlayerName = "Guest" + Math.floor(Math.random() * 1000);
  return prompt("Please enter your name", defaultPlayerName) || defaultPlayerName;
}


let socket = new WebSocket(`ws://${window.location.hostname}:8080/play`);

socket.onopen = function(e) {
    console.log('connection opened');
    sendJoinMessage(playerName, true);
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
  playingColor = msg.startFirst ? "black" : "white";

  document.getElementById("player-info").classList.add(playingColor);
  board.initColumns(msg.board, playingColor);
  board.draw();
}


function displayGameOver(msg) {
  alert(`${msg.winner} has won the game!`);
}

function selectMove(possibleMoves) {
  if (Object.keys(possibleMoves).length === 0) {
    return;
  }
  board.getPlayerMove(possibleMoves).then(selectedMove => {
    const msg = {
      type: "select-move",
      selectedMove
    };

    console.log("sending selected move: " + JSON.stringify(msg));

    socket.send(JSON.stringify(msg));
  });
}

function updateRoll(msg) {
  document.getElementById("roll-result").innerText = `${msg.dice1} ${msg.dice2} ${msg.playerName}`;
}

const diceButton = document.getElementById('roll');
diceButton.disabled = true;

function handleRollPrompt() {
  diceButton.disabled = false;
}

diceButton.addEventListener('click', () => {
  console.log("sending roll request")
  socket.send('{ "type": "roll" }');
  diceButton.disabled = true;
});
