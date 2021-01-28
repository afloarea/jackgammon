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
      
      case 'notify-roll':
        updateRoll(msg);
        break;
      case 'notify-move':
        updateMove(msg);
        break;
      case 'notify-end':
        displayGameOver(msg);
        break;
      default:
        break;
    }
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
  document.getElementById("player-info").classList.add(playingColor);
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

  document.getElementById("player-info").innerText = selectedPlayerName;

  sendJoinMessage(selectedPlayerName, true);
}

function displayGameOver(msg) {
  alert(`You ${playingColor === msg.winner ? 'won' : 'lost'} the game!`);
}

function selectMove(move) {
  const msg = {
    type: "select-move",
    playingColor,
    selectedMove: move
  };

  socket.send(JSON.stringify(msg));
}

function sendRoll() {
  console.log("sending roll request")
  socket.send(`{"type": "roll", "playingColor": "${playingColor}"}`);
}

function updateRoll(msg) {
  document.getElementById("roll-result").innerText = `${msg.dice1} ${msg.dice2} ${msg.playingColor}`;
}

//--------------------------------------
// handle update notification

function updateMove(msg) {
  const movingColor = msg.playingColor;
  const othetColor = movingColor === 'black' ? 'white' : 'black';
  const move = msg.move;

  if (move.type === "enter") {
    const suspendedElement = document.getElementById(`${movingColor}-suspended`);
    suspendedElement.innerText = parseInt(suspendedElement.innerText) - 1;

    const targetElement = document.getElementById(move.target.toString());
    if (targetElement.classList.contains(othetColor)) {
      targetElement.classList.remove(othetColor);
      targetElement.classList.add(movingColor);
      targetElement.innerText = 1;
      const otherSuspendedElement = document.getElementById(`${othetColor}-suspended`);
      otherSuspendedElement.innerText = parseInt(otherSuspendedElement.innerText) + 1;
    } else {
      targetElement.classList.add(movingColor);
      targetElement.innerText = parseInt(targetElement.innerText) + 1;
    }
  }

  if (move.type === "collect") {
    const sourceElement = document.getElementById(move.source.toString());
    const newSourceValue = parseInt(sourceElement.innerText) - 1;
    if (newSourceValue === 0) {
      sourceElement.classList.remove(movingColor);
    }
    sourceElement.innerText = newSourceValue;

    const collectedElement = document.getElementById(`${movingColor}-collected`);
    collectedElement.innerText = parseInt(collectedElement.innerText) + 1;
    return;
  }

  if (move.type === "simple") {
    const sourceElement = document.getElementById(move.source.toString());
    const newSourceValue = parseInt(sourceElement.innerText) - 1;
    if (newSourceValue === 0) {
      sourceElement.classList.remove(movingColor);
    }
    sourceElement.innerText = newSourceValue;

    const targetElement = document.getElementById(move.target.toString());
    if (targetElement.classList.contains(othetColor)) {
      targetElement.classList.remove(othetColor);
      targetElement.classList.add(movingColor);
      targetElement.innerText = 1;
      const otherSuspendedElement = document.getElementById(`${othetColor}-suspended`);
      otherSuspendedElement.innerText = parseInt(otherSuspendedElement.innerText) + 1;
    } else {
      targetElement.classList.add(movingColor);
      targetElement.innerText = parseInt(targetElement.innerText) + 1;
    }
  }


}

// -------------------------------------------

window.addEventListener('DOMContentLoaded', (event) => { // HTML LOADED
  // roll button
  document.getElementById("roll").addEventListener('click', () => sendRoll());

  



//---------------------------------------------
// button actions
let collectAction = false;
let enterAction = false;

document.getElementById('collect').addEventListener('click', () => collectAction = !collectAction);
document.getElementById('enter').addEventListener('click', () => enterAction = !enterAction);

let previusButton = null;

Array.from(document.getElementsByClassName('table-button')).forEach(button => button.addEventListener('click', () => {
  console.log('clicked: ' + button.id)

  if (collectAction) {
    sendSelectedMove(button.id, -1, "collect");
    collectAction = false;
    return;
  }

  if (enterAction) {
    sendSelectedMove(-1, button.id, "enter");
    enterAction = false;
    return;
  }

  if(previusButton === null) {
    previusButton = button;
    return;
  }

  sendSelectedMove(previusButton.id, button.id, "simple");
  previusButton = null;

}));

function sendSelectedMove(from, to, type) {
  const msg = {
    type: "select-move",
    playingColor: playingColor,
    selectedMove: {
      type,
      source: parseInt(from),
      target: parseInt(to)
    }
  };

  console.log("Sending move: " + JSON.stringify(msg));

  socket.send(JSON.stringify(msg));
}



//---------------------------------------------




















});


