async function displayPrompt() {
    return new Promise((resolve) => {
        vex.defaultOptions.className = 'vex-theme-os';
        vex.defaultOptions.showCloseButton = false;
        vex.defaultOptions.overlayClosesOnClick = false;
        vex.dialog.defaultOptions.buttons = [vex.dialog.buttons.YES];

        vex.dialog.open({
            message: 'Enter your name and game options:',
            input: [
                `<input name="name" type="text" placeholder="Your Name" value="Guest${Math.floor(Math.random() * 1000)}" required />`,
                '<div><input class="selection" type="radio" id="multi" name="play-mode" value="multiplayer"><label for="multi">Multiplayer</label></div>',
                '<div><input class="selection" type="radio" id="single" name="play-mode" value="computer" checked><label for="single">Singleplayer</label></div>',
                '<div><input class="selection" type="radio" id="private" name="play-mode" value="private"><label for="private">Keyword Based</label></div>',
                '<input class="hidden" id="keyword" name="keyword" type="text" placeholder="Keyword" value="all"/>'
            ].join(''),
            callback: function (data) {
                resolve(data);
            },
            afterOpen: function() {
                Array.from(document.getElementsByClassName('selection')).forEach(option => option.addEventListener('click', ev => {
                    const extraField = document.getElementById('keyword');
                    if(ev.target.id === 'private' && ev.target.checked) {
                        extraField.classList.remove('hidden');
                    } else {
                        extraField.classList.add('hidden');
                    }
                }));
            }
        });
    });
}
