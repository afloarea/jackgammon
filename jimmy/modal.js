export default async function displayPrompt() {

    const promptValues = getPromptInitialValues();
    initPrompt(promptValues);

    setPromptVisible();

    const options = await readInputs();
    setPromptVisible(false);
    saveOptionsAsCookies(options);
    return options;
}

async function readInputs() {
    return new Promise(resolve => {
        document.getElementById('confirm-btn').addEventListener('click', ev => {
            const inputs = {
                name: document.querySelector("input[name='name']").value,
                "play-mode": Array.from(document.getElementsByClassName('selection')).filter(element => element.checked)[0].id,
                keyword: document.getElementById('keyword').value
            };
            resolve(inputs);
        });
    });
}

function initPrompt(promptValues) {
    document.querySelector("input[name='name']").value = promptValues.name;

    const playModeId = promptValues['play-mode'];
    document.getElementById(playModeId).checked = 'true';

    const keywordInput = document.getElementById('keyword');
    keywordInput.value = promptValues.keyword;
    if (playModeId === 'private') {
        keywordInput.classList.remove('hidden');
    } else {
        keywordInput.classList.add('hidden');
    }

    Array.from(document.getElementsByClassName('selection')).forEach(option => option.addEventListener('click', ev => {
        const extraField = document.getElementById('keyword');
        if(ev.target.id === 'private' && ev.target.checked) {
            extraField.classList.remove('hidden');
        } else {
            extraField.classList.add('hidden');
        }
    }));
}

const prompt = document.getElementById('prompt');
prompt.addEventListener('animationend', (ev) => {
    console.info(ev);
    if (ev.animationName === 'dropOut') {
        prompt.style.display = 'none';    
    }
});

function setPromptVisible(visible=true) {
    const content = document.getElementsByClassName('modal-content')[0];
    content.classList.remove('dropOutAnimation', 'dropInAnimation');
    if (visible && prompt.style.display === 'block' || !visible && prompt.style.display === 'none') {
        return;
    }
    prompt.style.display = 'block';
    content.classList.add(visible ? 'dropInAnimation' : 'dropOutAnimation');
}

const POSSIBLE_PLAY_MODES = new Set(['multiplayer', 'random', 'neural', 'private']);

function getPromptInitialValues() {
    const cookies = readCookies();
    const playMode = cookies.get("play-mode");
    return {
        name: cookies.has("name") ? cookies.get("name") : `Guest${Math.floor(Math.random() * 1000)}`,
        "play-mode": POSSIBLE_PLAY_MODES.has(playMode) ? playMode : "random",
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