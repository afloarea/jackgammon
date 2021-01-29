const image = new Image();

const ctx = document.getElementsByTagName("canvas")[0].getContext('2d');

image.addEventListener('load', () => {
    ctx.drawImage(image, 0, 0, 40, 40);
})

image.src = "assets/white.png";