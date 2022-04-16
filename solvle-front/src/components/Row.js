import React, {useContext} from 'react';
import {AppContext} from "../App";
import Letter from "./Letter";

function Row({attempt}) {
    const {
        boardState
    } = useContext(AppContext);

    let letters = [];
    for(let i = 0; i < boardState.settings.wordLength; i++) {
        letters.push(<Letter key={"letter" + i + boardState.currAttempt.attempt} letterPos={i} attemptVal={attempt}/>);
    }

    return (
        <div className="solvle-row" id={boardState.currAttempt.attempt===attempt? "highlight" : ("row" + attempt)}>
            {letters}
        </div>
    );
}

export default Row;