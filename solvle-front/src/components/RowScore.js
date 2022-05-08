import React, {useContext} from 'react';
import AppContext from "../contexts/contexts";

function RowScore({rowNumber}) {
    const {
        rowScores
    } = useContext(AppContext);

    let fishing = rowScores[rowNumber] && rowScores[rowNumber].fishingScore ? (rowScores[rowNumber].fishingScore * 100).toFixed(0) + "%" : "";
    let remaining  = rowScores[rowNumber] && rowScores[rowNumber].remainingWords ? rowScores[rowNumber].remainingWords.toFixed(1) : "";

    return (
        <div className="rowScore" title={"This word scored " + fishing + " as a fishing word using the current configuration and would reduce the remaining words to an average of " + remaining + "."}>
            {rowNumber === 0 && <div>🐟✂</div> }
            <div>{fishing}</div>
            <div>{remaining}</div>
        </div>
    );
}

export default RowScore;