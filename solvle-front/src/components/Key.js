import React, {useContext} from "react";
import {AppContext} from "../App";

function Key({keyVal, bigKey, disabled}) {
    const {onSelectLetter, onDelete, onEnter, currentOptions} =
        useContext(AppContext);

    const selectLetter = () => {
        if (keyVal === "ENTER") {
            onEnter();
        } else if (keyVal === "DELETE") {
            onDelete();
        } else {
            onSelectLetter(keyVal);
        }
    };
    return (
        <div>
            <div
                className={"key" + (bigKey ? " big" : disabled ? " disabled" : "")}
                onClick={selectLetter}
            >
                {keyVal}
            </div>
            <div className="letterAmt" id={keyVal + "Amt"}>
                {bigKey ? "" : currentOptions.wordsWithCharacter[keyVal.toLowerCase()] > 0 ? currentOptions.wordsWithCharacter[keyVal.toLowerCase()] : 0}
            </div>
        </div>
    );
}

export default Key;