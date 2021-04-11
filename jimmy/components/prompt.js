async function displayPrompt() {
    return new Promise((resolve) => {
        vex.defaultOptions.className = 'vex-theme-os';
        vex.defaultOptions.showCloseButton = false;
        vex.defaultOptions.overlayClosesOnClick = false;
        vex.dialog.defaultOptions.buttons = [vex.dialog.buttons.YES];

        const values = getPromptInitialValues();
        const checked = ["", "", ""]
        switch (values['play-mode']) {
            case 'multiplayer': checked[0] = "checked";break;
            case 'computer':    checked[1] = "checked";break;
            case 'private':     checked[2] = "checked";break;
        }

        vex.dialog.open({
            message: 'Enter your name and game options:',
            input: [
                `<input name="name" type="text" placeholder="Your Name" value="${values.name}" required />`,
                `<div><input class="selection" type="radio" id="multi" name="play-mode" value="multiplayer" ${checked[0]}><label for="multi">Multiplayer</label></div>`,
                `<div><input class="selection" type="radio" id="single" name="play-mode" value="computer" ${checked[1]}><label for="single">Singleplayer</label></div>`,
                `<div><input class="selection" type="radio" id="private" name="play-mode" value="private" ${checked[2]}><label for="private">Keyword Based</label></div>`,
                `<input ${!checked[2] ? 'class="hidden"' : ''} id="keyword" name="keyword" type="text" placeholder="Keyword" value="${values.keyword}"/>`
            ].join(''),
            callback: function (data) {
                saveOptionsAsCookies(data);
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

function getPromptInitialValues() {
    const cookies = readCookies();
    return {
        name: cookies.has("name") ? cookies.get("name") : `Guest${Math.floor(Math.random() * 1000)}`,
        "play-mode": cookies.has("play-mode") ? cookies.get("play-mode") : "computer",
        keyword: cookies.has("keyword") ? cookies.get("keyword") : "all"
    };
}

function saveOptionsAsCookies(options) {
    Object.entries(options).forEach(entry => setCookie(entry[0], entry[1], 7));
}

function setCookie(name, value, expirationDays) {
    const date = new Date();
    date.setTime(date.getTime() + (expirationDays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + date.toUTCString();
    document.cookie = name + "=" + value + ";" + expires;
}

function readCookies() {
    const cookies = new Map();
    const decodedCookies = decodeURIComponent(document.cookie);
    decodedCookies.trim()
        .split(/\s*;\s*/)
        .filter(v => v.length !== 0)
        .forEach(cookiePair => {
            const splitIndex = cookiePair.indexOf("=");
            const key = cookiePair.substring(0, splitIndex);
            const value = cookiePair.substring(splitIndex + 1);
            cookies.set(key, value);
        });
    return cookies;
}