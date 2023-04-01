export function createWebSocket(config) {
    const socket = new WebSocket(config.url);

    socket.onopen = (ev) => config.handleOpen(ev, socket);

    socket.onmessage = function(event) {
        console.debug(event.data);
        const msg = JSON.parse(event.data);

        switch (msg.type) {
            case 'game-init':
            config.handleInit(msg, socket);
            break;
            case 'prompt-roll':
            config.handleRollPrompt(socket);
            break;
            case 'notify-roll':
            config.handleRollNotification(msg, socket);
            break;
            case 'prompt-move':
            config.handleMovePrompt(msg.possibleMoves, socket);
            break;
            case 'notify-move':
            config.handleMoveNotification(msg.move, socket);
            break;
            case 'notify-end':
            config.handleNotifyGameOver(msg, socket);
            break;
            case 'display-chat-message':
            config.handleDisplayChatEvent(msg, socket);
            break;
            default:
            console.log(`Unhandled message ${msg}`)
            break;
        }
    };

    socket.onclose = (ev) => config.handleClose(ev, socket);

    socket.onerror = (ev) => config.handleError(ev, socket);

    return socket;
}