export class Piece {
    x;
    y;

    radius;
    color;

    draw(ctx) {
        ctx.arc(this.x, this.y, this.radius, 0, 2 * Math.PI);
        ctx.fillStyle=this.color;
        ctx.fill();
    }
}

export class Column {
    id;
    direction;
    pieces;
    base;

    x;
    maxHeight;

    constructor(id, pieceCount, pieceColor, base, direction, x, maxHeight) {}

    removeTopPiece() {
        return this.pieces.pop();
    }

    getNextPieceSlot() {
        return [this.x, this.base + this.direction * 2 * Piece.radius * this.pieces.length];
    }
}

export class Board {
    columns;

    draw(ctx) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        this.columns.forEach(column => column.pieces.forEach(piece => piece.draw(ctx)));       
    }
}