import PROPS from './props.js';
import ASSETS from './assets.js';

export default class Piece {
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
