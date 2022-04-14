import React, {useContext} from "react";
import {AppContext} from "../App";

function Letter({letterPos, attemptVal}) {
    const {
        boardState,
        availableLetters, addAvailableLetter, removeAvailableLetter,
        unsureLetters, addUnsureLetter, removeUnsureLetter,
        knownLetters, addKnownLetter, removeKnownLetter
    } = useContext(AppContext);

    const letter = boardState.board[attemptVal][letterPos];
    const available = availableLetters.has(letter);
    const unsure = unsureLetters.get(letterPos).has(letter);
    const known = letter !== "" && knownLetters.get(letterPos) === letter;

    let letterState = "error";
    if (known) {
        letterState = "correct";
    } else if (unsure) {
        letterState = "almost";
    } else if (available) {
        letterState = "";
    } else if (letter === "") {
        letterState = "";
    }

    if (!available && (known || unsure)) {
        letterState = "huh";
    }

    const toggleState = () => {
        console.log("attemptVal " + attemptVal + " currAttempt " + boardState.currAttempt.attempt);
        if (attemptVal > boardState.currAttempt.attempt || letter === "") {
            return;
        }
        console.log("old state for " + letter + ": " + letterState);
        switch (letterState) {
            case "":
                console.log("adding error " + letter);
                removeKnownLetter(letterPos);
                removeAvailableLetter(letter);
                break;
            case "error":
                console.log("adding unsure " + letter);
                addAvailableLetter(letter);
                addUnsureLetter(letterPos, letter);
                break;
            case "almost":
                console.log("adding correct " + letter);
                removeUnsureLetter(letterPos, letter);
                addKnownLetter(letterPos, letter);
                break;
            case "correct":
                console.log("returning letter to default " + letter);
                removeKnownLetter(letterPos);
                break;
            default:
                console.log("clearing letter " + letter);
                removeKnownLetter(letterPos);
                removeUnsureLetter(letterPos, letter);
                break;
        }
    }

    return (
        <div className="letter" id={letterState} onClick={toggleState}>
            {letter}
        </div>
    );
}

export default Letter;