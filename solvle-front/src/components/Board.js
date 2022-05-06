import React, {useContext} from "react";
import AppContext from "../contexts/contexts";
import Row from "./Row";

function Board() {
    const {
        boardState,
    } = useContext(AppContext);

    let rows = [];
    for(let i = 0; i < boardState.settings.attempts; i++) {
        rows.push(<Row key={"row" + i} attempt={i} />);
    }

    return (
        <div className="board">
            {rows}
        </div>
    );
}

export default Board;