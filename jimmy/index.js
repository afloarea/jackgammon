import Board from './components/board/Board.js';
import { createWebSocket } from './components/game-socket.js';
import displayPrompt from './modal.js';

// this would be handled nicely by Vite
const BACKEND = `ws://${window.location.hostname}:8080/play`;

const diceButton = document.getElementById('roll');
diceButton.disabled = true;

const chatInput = document.getElementById('chat-input');
const chatButton = document.getElementById('chat-button');
chatButton.disabled = true;

const board = new Board();

let playerName;

displayPrompt().then(input => {
  
  const socketConfig = {
    url: BACKEND,
    handleOpen: function(e, socket) {
      console.log('connection opened');
      sendJoinMessage(input, socket);
    },
    handleClose: function(e) {
      if (e.wasClean) {
        console.log(`clean close - ${e.code}: ${e.reason}`);
      } else {
        console.log(`messy close - ${e.code}: ${e.reason}`);
      }
    },
    handleError: function(error) {
      console.error(error);
    },

    handleInit,
    handleRollPrompt,
    handleRollNotification: updateRoll,
    handleMovePrompt: selectMove,
    handleMoveNotification: (ev) => board.displayMove(ev),
    handleNotifyGameOver: displayGameOver,
    handleDisplayChatEvent
  };

  const socket = createWebSocket(socketConfig);
  diceButton.addEventListener('click', () => {
    console.log("sending roll request")
    socket.send('{ "type": "roll" }');
    diceButton.disabled = true;
  });

  chatButton.addEventListener('click', () => sendChatText(socket));
  chatButton.disabled = false;
  playerName = input.name;
  chatInput.addEventListener('keydown', (ev) => {
    if (ev.key === 'Enter') {
      sendChatText(socket);
    }
  })
});

function sendJoinMessage(input, socket) {
  const msg = {
    type: "join",
    playerName: input.name,
    options: {
      mode: input['play-mode'],
    }
  };
  if (input['play-mode'] === 'private') {
    msg.options.keyword = input.keyword;
  }

  socket.send(JSON.stringify(msg));
}

function handleInit(msg) {
  Array.from(document.getElementsByClassName(msg.startFirst ? "going-second":"going-first")).forEach(element => element.classList.add('hidden'));
  document.getElementById('player-name').innerText = msg.playerName;
  document.getElementById('opponent-name').innerText = msg.opponentName;
  
  document.getElementById('match-info').classList.remove('invisible');

  board.initColumns(msg.board, msg.startFirst ? "black":"white");
  board.draw();
}


function displayGameOver(msg) {
  alert(`${msg.winner} has won the game!`);
  window.location.reload();
}

function selectMove(possibleMoves, socket) {
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

function handleRollPrompt() {
  diceButton.disabled = false;
}

function handleDisplayChatEvent(event) {
  const textArea = document.querySelector('textarea');
  textArea.value += `${event.author}: ${event.message}\n`
  textArea.scrollTop = textArea.scrollHeight;
}

function sendChatText(socket) {
  const message = chatInput.value?.trim();
  if (!message) {
    return;
  }
  socket.send(JSON.stringify({ type: 'chat-message', author: playerName, message }));
  chatInput.value = null;
}
