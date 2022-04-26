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

    // prevent buttons from accidentally activating a second time when users press ENTER
    const releaseFocus = (e) => {
        if (e && e.target) {
            e.target.blur();
        }
    }
    const increaseWordLength = (e) => {
        if(boardState.settings.wordLength > 8) {
            return;
        }
        resetBoard(boardState.settings.attempts, boardState.settings.wordLength +1);
        releaseFocus(e);
    }
    const decreaseWordLength = (e) => {
        if(boardState.settings.wordLength < 3) {
            return;
        }
        resetBoard(boardState.settings.attempts, boardState.settings.wordLength -1);
        releaseFocus(e);
    }

    const increaseAttempts = (e) => {
        resetBoard(boardState.settings.attempts + 1, boardState.settings.wordLength);
        releaseFocus(e);
    }

    const decreaseAttempts = (e) => {
        if(boardState.settings.attempts < 2) {
            return;
        }
        resetBoard(boardState.settings.attempts - 1, boardState.settings.wordLength);
        releaseFocus(e);
    }

    const clickReset = (e) => {
        resetBoard(boardState.settings.attempts, boardState.settings.wordLength);
        releaseFocus(e);
    }

    const toggleResetWords = (event) => {
        console.log("Setting")
        setDictionary(event.target.value);
    }

    return (
        <div className="controls">
            <div className="flex-nowrap">
            <Button title="increase the maximum word length" variant="primary" onClick={increaseWordLength}>Letter +</Button>
            <Button title="decrease the maximum word length" variant="secondary" onClick={decreaseWordLength}>Letter -</Button>
            <Button title="increase the number of guesses allowed" variant="primary" onClick={increaseAttempts}>Row +</Button>
            <Button title="decrease the number of guesses allowed" variant="secondary" onClick={decreaseAttempts}>Row -</Button>
            <Button title="clear all letters from the board" variant="danger" onClick={clickReset}>Reset Board</Button>
            </div>
            <div onChange={toggleResetWords} className="wordLists">
                Word List:
                <span title="2315 words: https://github.com/techtribeyt/Wordle/blob/main/wordle_answers.txt Defaults to Scrabble for words that are not 5-letters.">
                    <input id="simpleRadio" defaultChecked={dictionary === "simple"} type="radio" value="simple" name="dict" />
                    <label htmlFor="simpleRadio">Simple</label>
                </span>
                <span title="172820 words: https://github.com/dolph/dictionary/blob/master/enable1.txt">
                    <input id="bigRadio" defaultChecked={dictionary === "big"} type="radio" value="big" name="dict" />
                    <label htmlFor="bigRadio">Scrabble</label>
                </span>
                <span title="370103 words: https://github.com/dwyl/english-words/blob/master/words_alpha.txt">
                    <input id="hugeRadio" defaultChecked={dictionary === "huge"} type="radio" value="huge" name="dict" />
                    <label htmlFor="hugeRadio">Huge</label>
                </span>
            </div>
        </div>
    );
}

export default Controls;