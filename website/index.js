const express = require("express");
const axios = require("axios");
require("dotenv").config();

const app = express();
const port = process.env.PORT;

app.use(express.json());
app.use(express.static("public"));

app.get("/", (req, res) => {
    res.sendFile("public/index.html");
});

app.post("/api/position", (req, res) => {
    let board = req.body.board;
    let player = req.body.player;
    if (player !== 'R' && player !== 'Y' || board.length !== 42 || !board.includes('-')) {
        res.json("Send a valid board.");
    } else {
        let matrix = new Array(6);
        for (let i = 0; i < 6; i++) {
            matrix[i] = new Array(7);
            for (let j = 0; j < 7; j++) {
                let value = board.charAt(i * 7 + j);
                if (value !== '-' && value !== 'R' && value !== 'Y') {
                    res.json("Send a valid board.");
                    return;
                }
                matrix[i][j] = value;
            }
        }
        let result = verify(matrix);
        if (!result) {
            res.json("Send a valid board.");
            return;
        }
        if (player === 'R') {
            board = board.replace(/R/g, 'X');
            board = board.replace(/Y/g, 'O');
        } else {
            board = board.replace(/Y/g, 'X');
            board = board.replace(/R/g, 'O');
        }
        axios.get(process.env.CONNECT_FOUR_CLOUD_FUNCTION + `?board=${board}`).then((resp) => {
            res.json({"move": Number(resp.data)});
        });
    }
});

function verify(matrix) {
    for (let i = 0; i < 7; i++) {
        let reached = false;
        for (let j = 0; j < 6; j++) {
            if (matrix[j][i] !== '-') {
                reached = true;
            }
            if (matrix[j][i] === '-' && reached) {
                return false;
            }
        }
    }
    return true;
}

app.listen(port);
