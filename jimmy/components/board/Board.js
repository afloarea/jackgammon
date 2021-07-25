import PROPS from './props.js';
import Column from './Column.js';
import anime from 'https://cdn.jsdelivr.net/npm/animejs@3.0.1/lib/anime.es.js';


function getMousePos(evt) {
    // TODO: check rect.left and rect.top are updated when the canvas is repositioned
    let rect = PROPS.canvas.getBoundingClientRect();
 
    let x = (evt.clientX - rect.left) / PROPS.canvas.clientWidth * PROPS.canvas.width;
    let y = (evt.clientY - rect.top) / PROPS.canvas.clientHeight * PROPS.canvas.height;
    // x = Math.ceil(x);
    // y = Math.ceil(y);
 
    return { x, y };
}

export default class Board {
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