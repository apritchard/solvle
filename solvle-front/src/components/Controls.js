import React, {useContext} from 'react';
import { Button} from 'react-bootstrap';
import {AppContext} from "../App";

function Controls() {
    const {
        boardState,
        restrictWords,
        setRestrictWords,
        resetBoard
    } = useContext(AppContext);

    const increaseWordLength = () => {
        if(boardState.settings.wordLength > 8) {
            return;
        }
        resetBoard(boardState.settings.attempts, boardState.settings.wordLength +1);
    }
    const decreaseWordLength = () => {
        if(boardState.settings.wordLength < 3) {
            return;
        }
        resetBoard(boardState.settings.attempts, boardState.settings.wordLength -1);
    }

    const increaseAttempts = () => {
        resetBoard(boardState.settings.attempts + 1, boardState.settings.wordLength);
    }

    const decreaseAttempts = () => {
        if(boardState.settings.attempts < 2) {
            return;
        }
        resetBoard(boardState.settings.attempts - 1, boardState.settings.wordLength);
    }

    const clickReset = () => {
        resetBoard(boardState.settings.attempts, boardState.settings.wordLength);
    }

    const toggleResetWords = () => {
        setRestrictWords(!restrictWords);
    }

    return (
        <div className="controls">
            <Button variant="primary" onClick={increaseWordLength}>Add Letter</Button>
            <Button variant="secondary" onClick={decreaseWordLength}>Remove Letter</Button>
            <Button variant="primary" onClick={increaseAttempts}>Add Row</Button>
            <Button variant="secondary" onClick={decreaseAttempts}>Remove Row</Button>
            <Button variant="danger" onClick={clickReset}>Reset Board</Button>
            <label title="Limits 5-character words to known Wordle wordlist">
                <input type="checkbox" checked={restrictWords} onChange={toggleResetWords}  />
                Restrict wordlist
            </label>
        </div>
    );
}

export default Controls;