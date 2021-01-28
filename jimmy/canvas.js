
const canvas = document.getElementById('game-canvas');
const ctx = canvas.getContext('2d');


const board = new Image();
// board.width=800;
// board.height=600;
board.onload = function() {
    console.log("got image");

    
    ctx.drawImage(board, 0, 0);

    const circle = new Image();
    circle.onload = function() {
        console.log("loaded circle");
        ctx.drawImage(circle, 10, 10);












    }
    circle.src = "circle-black.png";
}

board.src='scoreboard.png';

const circle = {
    x: 40,
    y: 40,
}

const circle2 = {
    x: 80,
    y: 40
}

function drawCircles() {
    ctx.beginPath();
    ctx.arc(circle.x, circle.y, 20, 0, 2 * Math.PI);
    ctx.stroke();

    ctx.beginPath();
    ctx.arc(circle2.x, circle2.y, 20, 0, 2 * Math.PI);
    ctx.stroke();
}

const timeline = anime.timeline({
    update() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        drawCircles();
    }
});

timeline.add({
    targets: circle,
    duration: 5_000,
    x: 40,
    y: 400,
    complete() {
        console.log("done animation 1");
    }
});
timeline.add({
    duration: 10_000,
    targets: circle2,
    x: 400,
    y: 40
}, 0);
