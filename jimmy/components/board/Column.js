import PROPS from './props.js';
import Piece from './Piece.js';

export default class Column {
    base;
    direction;
    x;
    pieces;
    maxPieceHeight;

    highlighted = false;


    constructor(base, direction, x, maxPieceHeight=PROPS.COLUMN_MAX_PIECE_HEIGHT) {
        this.base = direction < 0 ? base - PROPS.PIECE_DIAMETER : base;
        this.direction = direction;
        this.x = x;
        this.maxPieceHeight = maxPieceHeight;
        this.pieces = [];
    }

    init(pieceCount, pieceColor) {
        const pieceDistance = this._calculatePieceDistance(this.maxPieceHeight, pieceCount);
        for(let index = 0; index < pieceCount; index++) {
            const pieceX = this.x + PROPS.COLUMN_WIDTH / 2 - PROPS.PIECE_RADIUS;
            const pieceY = this.base + index * pieceDistance * this.direction;
            this.pieces.push(new Piece(pieceX, pieceY, pieceColor));
        }
    }

    _calculatePieceDistance(maxPieces, actualPieces) {
        if (maxPieces >= actualPieces) {
            return PROPS.PIECE_DIAMETER;
        }

        return (maxPieces - 1) / (actualPieces - 1) * PROPS.PIECE_DIAMETER;
    }

    removeTopPiece() {
        return this.pieces.pop();
    }

    getNewPiecePosition() {
        const pieceDistance = this._calculatePieceDistance(this.maxPieceHeight, this.pieces.length + 1);
        return { 
            x: this.x + PROPS.COLUMN_WIDTH / 2 - PROPS.PIECE_RADIUS, 
            y: this.base + this.direction * pieceDistance * this.pieces.length
        };
    }

    addTopPiece(piece) {
        this.pieces.push(piece);
    }

    generateShrinkAnimation() {
        if (this.pieces.length + 1 < this.maxPieceHeight) {
            return null;
        } 

        const newDistance = this._calculatePieceDistance(this.maxPieceHeight, this.pieces.length + 1);
        return this._generateAnimation(newDistance);
    }

    generateExpandAnimation() {
        if (this.pieces.length < this.maxPieceHeight) {
            return null;
        }

        const newDistance = this._calculatePieceDistance(this.maxPieceHeight, this.pieces.length);
        return this._generateAnimation(newDistance);
    }

    _generateAnimation(distance) {
        const self = this;
        return {
            targets: self.pieces,
            y: function(_, index) {
                return self.base + self.direction * distance * index;
            }
        };
    }

    containsCoordinates(mousePos) {
        //console.log(`x: ${x}, y: ${y}`);

        if (this.x + PROPS.COLUMN_WIDTH < mousePos.x || this.x - PROPS.COLUMN_WIDTH > mousePos.x) {
            return false;
        }
        if (this.direction > 0) {
            return this.base < mousePos.y && this.base + this.maxPieceHeight * PROPS.PIECE_DIAMETER + PROPS.PIECE_RADIUS > mousePos.y;
        } else {
            return this.base + PROPS.PIECE_DIAMETER > mousePos.y && this.base - (this.maxPieceHeight - 1) * PROPS.PIECE_DIAMETER - PROPS.PIECE_RADIUS < mousePos.y;
        }
    }

    //TODO: Remove this at some point
    draw() {
        PROPS.context.beginPath();
        const startY = this.direction === 1 ? this.base : this.base - (this.maxPieceHeight - 1) * PROPS.PIECE_DIAMETER;
        PROPS.context.rect(this.x, startY, PROPS.COLUMN_WIDTH, this.maxPieceHeight * PROPS.PIECE_DIAMETER);
        // PROPS.context.stroke(); // uncomment to display column area

        if (this.highlighted) {
            PROPS.context.fillStyle = PROPS.HIGHLIGHT;
            PROPS.context.fill();
        }
    }
}