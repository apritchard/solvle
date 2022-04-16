import React, {useContext} from 'react';
import { Button} from 'react-bootstrap';
import {AppContext} from "../App";

function Controls() {
    const {
        boardState,
        dictionary,
        setDictionary,
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

    const toggleResetWords = (event) => {
        console.log("Setting")
        setDictionary(event.target.value);
    }

    return (
        <div className="controls">
            <Button title="increase the maximum word length" variant="primary" onClick={increaseWordLength}>Add Letter</Button>
            <Button title="decrease the maximum word length" variant="secondary" onClick={decreaseWordLength}>Remove Letter</Button>
            <Button title="increase the number of guesses allowed" variant="primary" onClick={increaseAttempts}>Add Row</Button>
            <Button title="decrease the number of guesses allowed" variant="secondary" onClick={decreaseAttempts}>Remove Row</Button>
            <Button title="clear all letters from the board" variant="danger" onClick={clickReset}>Reset Board</Button>
            <div onChange={toggleResetWords}>
                Dictionary:
                <span title="2315 words: https://github.com/techtribeyt/Wordle/blob/main/wordle_answers.txt Only displays valid 5-letter wordle answers. Will use default dictionary for other word lengths.">
                    <input defaultChecked={dictionary === "wordle"} type="radio" value="wordle" name="dict" /> Wordle
                </span>
                <span title="108814 words: https://www.keithv.com/software/wlist/ match8 list"><input defaultChecked={dictionary === "default"} type="radio" value="default" name="dict" /> Default</span>
                <span title="172820 words: https://github.com/dolph/dictionary/blob/master/enable1.txt"><input defaultChecked={dictionary === "big"} type="radio" value="big" name="dict" /> Big</span>
                <span title="370103 words: https://github.com/dwyl/english-words/blob/master/words_alpha.txt"><input defaultChecked={dictionary === "huge"} type="radio" value="huge" name="dict" /> Huge</span>
            </div>
        </div>
    );
}

export default Controls;