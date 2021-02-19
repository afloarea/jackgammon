const PROPS = {};
PROPS.canvas = document.getElementById('game-canvas');
PROPS.context = PROPS.canvas.getContext('2d');
PROPS.canvasBounding = PROPS.canvas.getBoundingClientRect();
PROPS.WIDTH = PROPS.canvas.width;
PROPS.HEIGHT = PROPS.canvas.height;
PROPS.CANVAS_X = PROPS.canvasBounding.left;
PROPS.CANVAS_Y = PROPS.canvasBounding.top;
PROPS.PIECE_RADIUS = PROPS.WIDTH / (25 * 2);
PROPS.PIECE_DIAMETER = 2 * PROPS.PIECE_RADIUS;
PROPS.BOARD_BORDER_WIDTH = PROPS.WIDTH * 2 / 100;
PROPS.UPPER_BASE =  PROPS.HEIGHT * 2 / 100;
PROPS.LOWER_BASE = PROPS.HEIGHT - PROPS.UPPER_BASE;
PROPS.COLUMN_MAX_PIECE_HEIGHT = 7;
PROPS.PIx2 = 2 * Math.PI;
PROPS.BLACK = 'black';
PROPS.WHITE = 'yellow';
PROPS.HIGHLIGHT = '#A020F030';
PROPS.COLUMN_WIDTH = PROPS.WIDTH / 16;
PROPS.EDGE_WIDTH = PROPS.COLUMN_WIDTH;
PROPS.BOARD_SPLIT_WIDTH = 2 * PROPS.COLUMN_WIDTH;
PROPS.MIDDLE_WIDTH = 6 * PROPS.COLUMN_WIDTH;
PROPS.PLAYING_CLOCKWISE_TAGS = [
    'CW', ...'ABCDEF', 'SB', ...'GHIJKL',
    'CB', ...'MNOPQR', 'SW', ...'STUVWX'
];
PROPS.PLAYING_COUNTER_CLOCKWISE_TAGS = [
    'CB', ...'MNOPQR', 'SW', ...'STUVWX',
    'CW', ...'ABCDEF', 'SB', ...'GHIJKL'
];

PROPS.context.strokeStyle='purple'


function getMousePos(evt) {
    // TODO: check rect.left and rect.top are updated when the canvas is repositioned
    let rect = PROPS.canvas.getBoundingClientRect();
 
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
    image;

    constructor(x, y, color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.image = color == PROPS.WHITE ? ASSETS.WHITE_PIECE : ASSETS.BLACK_PIECE;
    } 

    draw() {
        PROPS.context.drawImage(this.image, this.x, this.y, PROPS.PIECE_DIAMETER, PROPS.PIECE_DIAMETER);
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

class Board {
    columnsById = new Map();
    movingPieces = [];

    static _buildColumns() {
        const columnLayout = [];
        
        let index = 0;
        // upper collect column
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, index++ * PROPS.COLUMN_WIDTH, 5));
        // upper left columns
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, index++ * PROPS.COLUMN_WIDTH));

        // upper suspend column
        columnLayout.push(new Column(PROPS.HEIGHT / 3, -1, PROPS.WIDTH / 2 - PROPS.COLUMN_WIDTH / 2, 5));

        // upper right columns
        index = 7;
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.UPPER_BASE, 1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));

        index = 0;
        // lower collect column
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, index++ * PROPS.COLUMN_WIDTH, 5));
        // lower left columns
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, index++ * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, index++ * PROPS.COLUMN_WIDTH));

        // lower suspend column
        columnLayout.push(new Column(PROPS.HEIGHT * 2 / 3, 1, PROPS.WIDTH / 2 - PROPS.COLUMN_WIDTH / 2, 5));

        // lower right columns
        index = 7;
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));
        columnLayout.push(new Column(PROPS.LOWER_BASE, -1, PROPS.WIDTH - index-- * PROPS.COLUMN_WIDTH));

        return columnLayout;
    }

    _initColumnListener() {
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

    initColumns(boardColumns, playingColor) {
        let mainColor;
        let secondColor;
        let columnTags;
        if (playingColor === 'black') {
            mainColor = PROPS.BLACK;
            secondColor = PROPS.WHITE;
            columnTags = PROPS.PLAYING_CLOCKWISE_TAGS;
        } else {
            mainColor = PROPS.WHITE;
            secondColor = PROPS.BLACK;
            columnTags = PROPS.PLAYING_COUNTER_CLOCKWISE_TAGS;
        }

        const generatedColumns = Board._buildColumns();
        for (let index = 0; index < columnTags.length; index++) {
            this.columnsById.set(columnTags[index], generatedColumns[index]);
        }
        this._initColumnListener();

        boardColumns.player.forEach(column => this.columnsById.get(column.columnId).init(column.pieces, mainColor));
        boardColumns.opponent.forEach(column => this.columnsById.get(column.columnId).init(column.pieces, secondColor));
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