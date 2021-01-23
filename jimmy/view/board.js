const PROPS = {};
PROPS.canvas = document.getElementById('game-canvas');
PROPS.context = PROPS.canvas.getContext('2d');
PROPS.canvasBounding = PROPS.canvas.getBoundingClientRect();
PROPS.WIDTH = PROPS.canvas.width;
PROPS.HEIGHT = PROPS.canvas.height;
PROPS.CANVAS_X = PROPS.canvasBounding.left;
PROPS.CANVAS_Y = PROPS.canvasBounding.top;
PROPS.PIECE_RADIUS = PROPS.WIDTH / (15 * 2);
PROPS.PIECE_DIAMETER = 2 * PROPS.PIECE_RADIUS;
PROPS.UPPER_BASE = PROPS.PIECE_RADIUS;
PROPS.LOWER_BASE = PROPS.HEIGHT - PROPS.PIECE_RADIUS;
PROPS.COLUMN_MAX_PIECE_HEIGHT = 7;
PROPS.PIx2 = 2 * Math.PI;
PROPS.BLACK = 'black';
PROPS.WHITE = 'yellow';
PROPS.HIGHLIGHT = 'purple';

PROPS.context.strokeStyle='purple'


function getMousePos(evt) {
    // TODO: check rect.left and rect.top are updated when the canvas is repositioned
    let rect = PROPS.canvasBounding;
 
    let X = (evt.clientX - rect.left) / PROPS.canvas.clientWidth * PROPS.canvas.width;
    let Y = (evt.clientY - rect.top) / PROPS.canvas.clientHeight * PROPS.canvas.height;
    // X = Math.ceil(X);
    // Y = Math.ceil(Y);
 
    return {
        x: X,
        y: Y
    };
}


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
        PROPS.context.fillStyle = this.color;
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

    highlighted = false;


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

    containsCoordinates(mousePos) {
        //console.log(`x: ${x}, y: ${y}`);

        if (this.x + PROPS.PIECE_RADIUS < mousePos.x || this.x - PROPS.PIECE_RADIUS > mousePos.x) {
            return false;
        }
        if (this.direction > 0) {
            return this.base - PROPS.PIECE_RADIUS < mousePos.y && this.base + this.maxPieceHeight * PROPS.PIECE_DIAMETER + PROPS.PIECE_RADIUS > mousePos.y;
        } else {
            return this.base + PROPS.PIECE_RADIUS > mousePos.y && this.base - this.maxPieceHeight * PROPS.PIECE_DIAMETER - PROPS.PIECE_RADIUS < mousePos.y;
        }
    }

    //TODO: Remove this at some point
    draw() {
        PROPS.context.beginPath();
        PROPS.context.moveTo(this.x - PROPS.PIECE_RADIUS, this.base - this.direction * PROPS.PIECE_RADIUS);
        PROPS.context.lineTo(this.x, this.base + this.direction * this.maxPieceHeight * PROPS.PIECE_DIAMETER);
        PROPS.context.lineTo(this.x + PROPS.PIECE_RADIUS, this.base - this.direction * PROPS.PIECE_RADIUS);
        PROPS.context.closePath();
        PROPS.context.stroke();
        if (this.highlighted) {
            PROPS.context.fillStyle = PROPS.HIGHLIGHT;
            PROPS.context.fill();
        }
        
    }
}

class Board {
    columnsById = new Map();
    movingPieces = [];

    constructor() {
        let index = 0;
        this.columnsById.set('CW', new Column(PROPS.UPPER_BASE + 2 * PROPS.PIECE_DIAMETER, 1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER, 5));
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
        this.columnsById.set('CB', new Column(PROPS.LOWER_BASE - 2 * PROPS.PIECE_DIAMETER, -1, PROPS.PIECE_RADIUS + index++ * PROPS.PIECE_DIAMETER, 5));
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

        PROPS.canvas.addEventListener('click', (ev) => {
            if (this.columnClickListener === null) {
                return;
            }

            for (let columnEntry of this.columnsById.entries()) {
                if (columnEntry[1].containsCoordinates(getMousePos(ev))) {
                    console.log("selected column " + columnEntry[0]);
                    this.columnClickListener(columnEntry[0]);
                    return;
                }
            }
            this.columnClickListener(null);
        });
    }

    initColumns(boardColumns) {
        boardColumns.forEach(column => {
            this.columnsById.get(column.columnId).init(column.pieces, column.color === 'black' ? PROPS.BLACK : PROPS.WHITE);
        });

        // maybe remove this? or move it somewhere else
        this.draw();
    }


    draw() {
        PROPS.context.clearRect(0, 0, PROPS.WIDTH, PROPS.HEIGHT);
        for (let column of this.columnsById.values()) {
            column.draw();
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


    async getPlayerMove(possibleMoves) {
        const sourceColumns = Object.keys(possibleMoves);
        if (sourceColumns.length === 0) {
            return;
        }

        const sourceColumn = await this.getColumnInput(sourceColumns);
        if (sourceColumn === null) {
            return this.getPlayerMove(possibleMoves);
        }
        const targetColumn = await this.getColumnInput(possibleMoves[sourceColumn]);
        if (targetColumn === null) {
            return this.getPlayerMove(possibleMoves);
        }

        return { source: sourceColumn, target: targetColumn };
    }

    columnClickListener = null;

    async getColumnInput(possibleColumns) {
        const self = this;
        return new Promise((resolve) => {

            // highlight
            possibleColumns.map(columnId => this.columnsById.get(columnId)).forEach(column => column.highlighted = true);
            self.draw();
            console.log('highlight columns: ' + possibleColumns);
            this.columnClickListener = function(selectedColumn) {
                // remove highlight
                possibleColumns.map(columnId => this.columnsById.get(columnId)).forEach(column => column.highlighted = false);
                self.draw();
                this.columnClickListener = null;
                resolve(possibleColumns.includes(selectedColumn) ? selectedColumn : null);
            }

        });
    }


}