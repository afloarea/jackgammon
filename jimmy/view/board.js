const PROPS = {};
PROPS.canvas = document.getElementById('game-canvas');
PROPS.context = PROPS.canvas.getContext('2d');
PROPS.WIDTH = PROPS.canvas.width;
PROPS.HEIGHT = PROPS.canvas.height;
PROPS.PIECE_RADIUS = PROPS.WIDTH / (15 * 2);
PROPS.UPPER_BASE = PROPS.PIECE_RADIUS;
PROPS.LOWER_BASE = PROPS.HEIGHT - PROPS.PIECE_RADIUS;
PROPS.COLUMN_MAX_HEIGHT = PROPS.HEIGHT / 3;
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
    maxHeight;


    constructor(base, direction, x, pieceCount=0, pieceColor=null, maxHeight=PROPS.maxHeight) {
        this.base = base;
        this.direction = direction;
        this.x = x;
        this.maxHeight = maxHeight;
        this.pieces = [];

        for(let index = 0; index < pieceCount; index++) {
            this.pieces.push(new Piece(this.x, this.base + index * 2 * PROPS.PIECE_RADIUS * direction, pieceColor));
        }
    }

    removeTopPiece() {
        return this.pieces.pop();
    }

    getNewPiecePosition() {
        return { x: this.x, y: this.base + this.direction * 2 * PROPS.PIECE_RADIUS * this.pieces.length };
    }

    addTopPiece(piece) {
        this.pieces.push(piece);
    }
}

class Board {
    columnsById = new Map();
    allPieces;

    initColumns() {
        let index = 0;
        this.columnsById.set('A', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 2, PROPS.BLACK));
        this.columnsById.set('B', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('C', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('D', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('E', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('F', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 5, PROPS.WHITE));

        this.columnsById.set('SB', new Column(PROPS.UPPER_BASE + 2 * 2 * PROPS.PIECE_RADIUS, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 0, null, PROPS.maxHeight / 2));

        this.columnsById.set('G', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('H', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 3, PROPS.WHITE));
        this.columnsById.set('I', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('J', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('K', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('L', new Column(PROPS.UPPER_BASE, 1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 5, PROPS.BLACK));


        index = 0;
        this.columnsById.set('M', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 2, PROPS.WHITE));
        this.columnsById.set('N', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('O', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('P', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('Q', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('R', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 5, PROPS.BLACK));

        this.columnsById.set('SW', new Column(PROPS.LOWER_BASE - 2 * 2 * PROPS.PIECE_RADIUS, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 0, null, PROPS.maxHeight / 2));

        this.columnsById.set('S', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('T', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 3, PROPS.BLACK));
        this.columnsById.set('U', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('V', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('W', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS));
        this.columnsById.set('X', new Column(PROPS.LOWER_BASE, -1, PROPS.PIECE_RADIUS + index++ * 2 * PROPS.PIECE_RADIUS, 5, PROPS.WHITE));

        this.allPieces = Array.from(this.columnsById.values()).reduce((acc, column) => acc.concat(column.pieces), []);
    }


    draw() {
        PROPS.context.clearRect(0, 0, PROPS.WIDTH, PROPS.HEIGHT);
        this.allPieces.forEach(piece => piece.draw()); 
    }

    displayMove(move) {
        //TODO: calling twice before animation is complete
        const sourceColumn = this.columnsById.get(move.source);
        const targetColumn = this.columnsById.get(move.target);

        const movedPiece = sourceColumn.removeTopPiece();
        const newPosition = targetColumn.getNewPiecePosition();
        const self = this;
        anime({
            targets: movedPiece,
            x: newPosition.x,
            y: newPosition.y,
            duration: 1_200,
            easing: 'easeOutCubic',
            update() {
                self.draw();
            },
            complete() {
                console.log("done");
                targetColumn.addTopPiece(movedPiece);
            }
        });
    }
}

const board = new Board();
board.initColumns();
board.draw();