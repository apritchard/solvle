import React, {useContext} from 'react';
import AppContext from "../contexts/contexts";
import Letter from "./Letter";
import RowScore from "./RowScore";

function Row({attempt}) {
    const {
        boardState
    } = useContext(AppContext);

    let letters = [];
    for(let i = 0; i < boardState.settings.wordLength; i++) {
        letters.push(<Letter key={"letter" + i + boardState.currAttempt.attempt} letterPos={i} attemptVal={attempt}/>);
    }
    if(boardState.settings.rateEnteredWords === true) {
        letters.push(<RowScore rowNumber={attempt} />);
    }

    return (
        <div className="solvle-row" id={boardState.currAttempt.attempt===attempt? "highlight" : ("row" + attempt)}>
            {letters}
        </div>
    );
}

export default Row;