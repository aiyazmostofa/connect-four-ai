const ROWS = 6;
const COLUMNS = 7;

const BACKGROUND = "rgb(120,160,200)";
const BLUE = "rgb(0,35,149)";
const RED = "rgb(215,0,0)";
const GREEN = "rgb(0,255,0)";
const TRANS_RED = "rgba(215,0,0,0.5)";
const YELLOW = "rgb(255,215,0)";
const TRANS_YELLOW = "rgba(255,215,0,0.5)";

let matrix;
let blankSpots;
let canvas;
let currentYellow;
let mouseX, mouseY;
let mouseIn;
let nonActive;
let userPlayer;
let gameId;
let lastPlayedColumn;
let lastPlayedRow;
let moveNumber;

function init() {
    canvas = document.getElementById("game");
    injectListeners();
    nonActive = true;
    userPlayer = 'Y';
    gameId = 0;
    setNewGame();
    render();
}

function injectListeners() {
    canvas.addEventListener("pointerdown", registerMouseClick);
    canvas.addEventListener("pointermove", updateMousePosition);
    canvas.addEventListener("mouseenter", () => {
        mouseIn = true;
        render();
    });
    canvas.addEventListener("mouseleave", () => {
        mouseIn = false;
        render();
    });
}

function setNewGame() {
    matrix = new Array(ROWS);
    blankSpots = new Array(COLUMNS);
    for (let i = 0; i < COLUMNS; i++) blankSpots[i] = ROWS - 1;
    for (let i = 0; i < ROWS; i++) {
        matrix[i] = new Array(COLUMNS);
        for (let j = 0; j < COLUMNS; j++) {
            matrix[i][j] = '-';
        }
    }
    moveNumber = 0;
    currentYellow = true;
    mouseIn = false;
    lastPlayedColumn = 0;
    lastPlayedRow = 0;
}

function updateMousePosition(e) {
    let rect = canvas.getBoundingClientRect();
    mouseX = e.x - rect.x;
    mouseY = e.y - rect.y;
    render();
}

function registerMouseClick(e) {
    if (nonActive) return;
    if (currentYellow && userPlayer === 'R' || !currentYellow && userPlayer === 'Y') return;
    let rect = canvas.getBoundingClientRect();
    let x = e.x - rect.x;
    let y = e.y - rect.y;
    for (let i = 0; i < COLUMNS; i++) {
        if (x >= i * 100 + 10 && x < i * 100 + 110) {
            for (let j = 0; j < ROWS; j++) {
                if (y >= j * 100 + 10 && y <= j * 100 + 110) {
                    if (inCircle(i * 100 + 60, j * 100 + 60, 40, x, y)) performPlayerMove(userPlayer, i);
                    return;
                }
            }
        }
    }
}

function performPlayerMove(player, index) {
    if (blankSpots[index] === -1) return;
    lastPlayedRow = blankSpots[index];
    lastPlayedColumn = index;
    matrix[blankSpots[index]--][index] = player === 'Y' ? 'Y' : 'R';
    currentYellow = !currentYellow;
    moveNumber++;
    let result = checkWinner();
    let type = result[0];
    if (type === 'R' || type === 'Y' || type === 'T') {
        endGame(result);
        render();
        return;
    }
    updatePlayerGUIDuringGame();
    if (userPlayer === 'Y' && !currentYellow || userPlayer === 'R' && currentYellow) {
        aiMove();
    }
    render();
}

function aiMove() {
    let time = gameId;
    output("Waiting for AI to respond.")
    let move;
    function apiHandle() {
        if (gameId !== time) return;
        output("Your turn to move.");
        performPlayerMove(userPlayer === 'Y' ? 'R' : 'Y', move);
    }

    let string = "";
    for (let i = 0; i < ROWS; i++) {
        for (let j = 0;j < COLUMNS; j++) {
            string += matrix[i][j];
        }
    }

    fetch("https://connect-four-ai.up.railway.app/api/position", {
        method: "POST",
        body: JSON.stringify({
            board: string,
            player: userPlayer === 'Y' ? 'R' : 'Y'
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    })
        .then((response) => response.json())
        .then((json) => move = json.move)
        .then(apiHandle);
}

function updatePlayerGUIDuringGame() {
    let playerButton = document.getElementById("player-button");
    playerButton.disabled = true;
    playerButton.className = "player-button-disabled";
    playerButton.innerText = "Started";
}

function updatePlayerGUIBeforeGame() {
    let playerButton = document.getElementById("player-button");
    playerButton.disabled = false;
    if (userPlayer === 'Y') {
        playerButton.className = "player-button-yellow";
        playerButton.innerText = "You're 1st";
    } else {
        playerButton.className = "player-button-red";
        playerButton.innerText = "You're 2nd";
    }
}

function updateGameGUI() {
    let gameButton = document.getElementById("game-button");
    gameButton.innerText = nonActive ? "Start Game" : "Stop Game";
}
function toggleUserPlayer() {
    userPlayer = userPlayer === 'Y' ? 'R' : 'Y';
    updatePlayerGUIBeforeGame();
}

function endGame(result) {
    nonActive = true;
    updatePlayerGUIBeforeGame();
    updateGameGUI();
    if (result[0] === 'T') {
        output("Game ended in a tie.");
        return;
    }
    let list = result[1];
    for (let i = 0; i < list.length; i++) {
        matrix[list[i][0]][list[i][1]] = 'H' + matrix[list[i][0]][list[i][1]];
    }
    if (result[0] === 'Y') {
        if (userPlayer === 'Y') output("You (yellow) won.");
        else output("The AI (yellow) won.");
    } else {
        if (userPlayer === 'R') output("You (red) won.");
        else output("The AI (red) won.");
    }
}

function handleGameStateChange() {
    gameId++;
    if (nonActive) {
        nonActive = false;
        setNewGame();
        updateGameGUI();
        updatePlayerGUIDuringGame();
        render();
        if (userPlayer === 'R') aiMove();
        if (userPlayer === 'Y') output("Your turn to move.");
    } else {
        nonActive = true;
        updateGameGUI();
        updatePlayerGUIBeforeGame();
        output("Click start to play!");
        render();
    }
}

function render() {
    if (canvas.getContext) {
        const ctx = canvas.getContext("2d");
        ctx.fillStyle = BLUE;
        ctx.fillRect(0, 0, 720, 620);
        for (let i = 0; i < ROWS; i++) {
            for (let j = 0; j < COLUMNS; j++) {
                let value = matrix[i][j];
                let highlight = false;
                if (value.charAt(0) === 'H') {
                    highlight = true;
                    value = value.substring(1);
                }

                if (value === '-') {
                    ctx.fillStyle = BACKGROUND;
                } else if (value === 'R') {
                    ctx.fillStyle = RED;
                } else {
                    ctx.fillStyle = YELLOW;
                }
                ctx.beginPath();
                ctx.arc(j * 100 + 60, i * 100 + 60, 40, 0, 2 * Math.PI, true);
                ctx.fill();

                if (highlight) {
                    ctx.strokeStyle = GREEN;
                    ctx.lineWidth = 5;
                    ctx.beginPath();
                    ctx.arc(j * 100 + 60, i * 100 + 60, 40, 0, 2 * Math.PI, true);
                    ctx.stroke();
                }
            }
        }

        if (!mouseIn) return;

        let c;
        for (let i = 0; i < COLUMNS; i++) {
            if (mouseX >= i * 100 + 10 && mouseX < i * 100 + 110) {
                c = i;
                break;
            }
        }

        let r;
        for (let i = 0; i < ROWS; i++) {
            if (mouseY >= i * 100 + 10 && mouseY < i * 100 + 110) {
                r = i;
                break;
            }
        }

        if (r > blankSpots[c] || !inCircle(c * 100 + 60, r * 100 + 60, 40, mouseX, mouseY)) return;

        ctx.fillStyle = userPlayer === 'Y' ? TRANS_YELLOW : TRANS_RED;
        ctx.beginPath();
        ctx.arc(c * 100 + 60, r * 100 + 60, 40, 0, 2 * Math.PI, true);
        ctx.fill();
    }
}

function inCircle(x, y, r, a, b) {
    return Math.sqrt((x - a) * (x - a) + (y - b) * (y - b)) <= r;
}

function checkWinner() {
    let red = 0;
    let yellow = 0;
    let list = [];

    for (let i = -3; i <= 3; i++) {
        if (i + lastPlayedColumn < 0 || i + lastPlayedColumn >= 7) continue;
        if (matrix[lastPlayedRow][i + lastPlayedColumn] === 'R') {
            red++;
            yellow = 0;
        }
        if (matrix[lastPlayedRow][i + lastPlayedColumn] === 'Y') {
            red = 0;
            yellow++;
        }

        if (matrix[lastPlayedRow][i + lastPlayedColumn] === '-') {
            red = yellow = 0;
        }
        if (red === 4) {
            for (let j = i - 3; j <= i; j++) list.push([lastPlayedRow, lastPlayedColumn + j]);
            return ['R', list];
        }
        if (yellow === 4) {
            for (let j = i - 3; j <= i; j++) list.push([lastPlayedRow, lastPlayedColumn + j]);
            return ['Y', list];
        }
    }

    red = yellow = 0;

    for (let i = -3; i <= 3; i++) {
        if (i + lastPlayedRow < 0 || i + lastPlayedRow >= 6) continue;
        if (matrix[lastPlayedRow + i][lastPlayedColumn] === 'R') {
            red++;
            yellow = 0;
        }
        if (matrix[lastPlayedRow + i][lastPlayedColumn] === 'Y') {
            red = 0;
            yellow++;
        }

        if (matrix[lastPlayedRow + i][lastPlayedColumn] === '-') {
            red = yellow = 0;
        }
        if (red === 4) {
            for (let j = i - 3; j <= i; j++) list.push([lastPlayedRow + j, lastPlayedColumn]);
            return ['R', list];
        }

        if (yellow === 4) {
            for (let j = i - 3; j <= i; j++) list.push([lastPlayedRow + j, lastPlayedColumn]);
            return ['Y', list];
        }
    }

    red = yellow = 0;

    for (let i = -3; i <= 3; i++) {
        if (i + lastPlayedRow < 0 || i + lastPlayedRow >= 6 || lastPlayedColumn + i < 0 || lastPlayedColumn + i >= 7) continue;
        if (matrix[lastPlayedRow + i][lastPlayedColumn + i] === 'R') {
            red++;
            yellow = 0;
        }
        if (matrix[lastPlayedRow + i][lastPlayedColumn + i] === 'Y') {
            red = 0;
            yellow++;
        }
        if (matrix[lastPlayedRow + i][lastPlayedColumn + i] === '-') {
            red = yellow = 0;
        }

        if (red === 4) {
            for (let j = i - 3; j <= i; j++) list.push([lastPlayedRow + j, lastPlayedColumn+j]);
            return ['R', list];
        }

        if (yellow === 4) {
            for (let j = i - 3; j <= i; j++) list.push([lastPlayedRow + j, lastPlayedColumn + j]);
            return ['Y', list];
        }
    }

    red = yellow = 0;

    for (let i = -3; i <= 3; i++) {
        if (i + lastPlayedRow < 0 || i + lastPlayedRow >= 6 || lastPlayedColumn - i < 0 || lastPlayedColumn - i >= 7) continue;
        if (matrix[lastPlayedRow + i][lastPlayedColumn - i] === 'R') {
            red++;
            yellow = 0;
        }
        if (matrix[lastPlayedRow + i][lastPlayedColumn - i] === 'Y') {
            red = 0;
            yellow++;
        }
        if (matrix[lastPlayedRow + i][lastPlayedColumn - i] === '-') {
            red = yellow = 0;
        }
        if (red === 4) {
            for (let j = i - 3; j <= i; j++) list.push([lastPlayedRow + j, lastPlayedColumn-j]);
            return ['R', list];
        }

        if (yellow === 4) {
            for (let j = i - 3; j <= i; j++) list.push([lastPlayedRow + j, lastPlayedColumn - j]);
            return ['Y', list];
        }
    }
    return [moveNumber === 42 ? 'T' : 'U'];
}

function output(s) {
    document.getElementById("output").innerText = s;
}

init();