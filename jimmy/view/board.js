const PROPS = {};
PROPS.canvas = document.getElementById('game-canvas');
PROPS.context = PROPS.canvas.getContext('2d');
PROPS.WIDTH = PROPS.canvas.width;
PROPS.HEIGHT = PROPS.canvas.height;
PROPS.PIECE_RADIUS = PROPS.WIDTH / (15 * 2);
PROPS.PIECE_DIAMETER = 2 * PROPS.PIECE_RADIUS;
PROPS.UPPER_BASE = PROPS.PIECE_RADIUS;
PROPS.LOWER_BASE = PROPS.HEIGHT - PROPS.PIECE_RADIUS;
PROPS.COLUMN_MAX_PIECE_HEIGHT = 7;
PROPS.PIx2 = 2 * Math.PI;
PROPS.BLACK = 'black';
PROPS.WHITE = 'yellow';

PROPS.context.strokeStyle='purple'


class Piece {
    x;
    y;
    color;

    constructor(x, y, color) {
        this.x = x;
        this.y = y;
        this.color = color;
    } 

    draw() {
        PROPS.context.beginPath();
        PROPS.context.arc(this.x, this.y, PROPS.PIECE_RADIUS, 0, PROPS.PIx2);
        PROPS.context.fillStyle=this.color;
        PROPS.context.fill();
        PROPS.context.stroke();
    }
}

class Column {
    base;
    direction;
    x;
    pieces;
    maxPieceHeight;


    constructor(base, direction, x, maxPieceHeight=PROPS.COLUMN_MAX_PIECE_HEIGHT) {
        this.base = base;
        this.direction = direction;
        this.x = x;
        this.maxPieceHeight = maxPieceHeight;
        this.pieces = [];
    }

    init(pieceCount, pieceColor) {
        const pieceDistance = this._calculatePieceDistance(this.maxPieceHeight, pieceCount);
        for(let index = 0; index < pieceCount; index++) {
            this.pieces.push(new Piece(this.x, this.base + index * pieceDistance * this.direction, pieceColor));
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
        return { x: this.x, y: this.base + this.direction * pieceDistance * this.pieces.length };
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
}

class Board {
    columnsById = new Map();
    movingPieces = [];

    constructor() {
        let index = 0;
        this.columnsById.set('A', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('B', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('C', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('D', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('E', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('F', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));

        this.columnsById.set('SB', new Column(PROPS.UPPER_BASE + 2 * PROPS.PIECE_DIAMETER, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER, 5));

        this.columnsById.set('G', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('H', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('I', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('J', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('K', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('L', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));


        index = 0;
        this.columnsById.set('M', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('N', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('O', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('P', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('Q', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('R', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));

        this.columnsById.set('SW', new Column(PROPS.LOWER_BASE - 2 * PROPS.PIECE_DIAMETER, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER, 5));

        this.columnsById.set('S', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('T', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('U', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('V', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('W', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
        this.columnsById.set('X', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER));
    }

    initColumns() {
        this.columnsById.get('A').init(2, PROPS.BLACK);
        this.columnsById.get('F').init(5, PROPS.WHITE);
        this.columnsById.get('H').init(3, PROPS.BLACK);
        this.columnsById.get('L').init(5, PROPS.WHITE);
        this.columnsById.get('M').init(2, PROPS.BLACK);
        this.columnsById.get('R').init(5, PROPS.WHITE);
        this.columnsById.get('T').init(3, PROPS.BLACK);
        this.columnsById.get('X').init(5, PROPS.WHITE);
    }


    draw() {
        PROPS.context.clearRect(0, 0, PROPS.WIDTH, PROPS.HEIGHT);
        for (let column of this.columnsById.values()) {
            column.pieces.forEach(piece => piece.draw());
        }
        this.movingPieces.forEach(piece => piece.draw());
    }

    previousAnimation = Promise.resolve();

    displayMove(move) {
        this.previousAnimation = this.previousAnimation.then(() => this.animateMove(move));
    }

    async animateMove(move) {
        return new Promise(resolve => {
            const sourceColumn = this.columnsById.get(move.source);
            const targetColumn = this.columnsById.get(move.target);

            const movedPiece = sourceColumn.removeTopPiece();
            this.movingPieces.push(movedPiece);
            const newPosition = targetColumn.getNewPiecePosition();
            const self = this;

            const timeline = anime.timeline({
                duration: 1_200,
                easing: 'easeOutCubic',
                update() {
                    self.draw();
                },
                complete() {
                    console.log("done");
                    targetColumn.addTopPiece(movedPiece);
                    self.movingPieces.pop();
                    resolve();
                }
            });

            timeline.add({
                targets: movedPiece,
                x: newPosition.x,
                y: newPosition.y
            });

            const expandAnimation = sourceColumn.generateExpandAnimation();
            const shrinkAnimation = targetColumn.generateShrinkAnimation();

            if (expandAnimation != null) {
                timeline.add(expandAnimation, 0);
            }
            
            if (shrinkAnimation != null) {
                timeline.add(shrinkAnimation, 0);
            }
        });
    }
}

const board = new Board();
board.initColumns();
board.draw();