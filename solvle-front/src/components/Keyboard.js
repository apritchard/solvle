import React, {useCallback, useEffect, useContext} from "react";
import Key from "./Key";
import AppContext from "../contexts/contexts";

function Keyboard() {
    const keys1 = ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"];
    const keys2 = ["A", "S", "D", "F", "G", "H", "J", "K", "L"];
    const keys3 = ["Z", "X", "C", "V", "B", "N", "M"];

    const {
        availableLetters,
        onSelectLetter,
        onEnter,
        onDelete,
    } = useContext(AppContext);

    const handleKeyboard = useCallback(
        (event) => {
            if (event.key === "Enter") {
                onEnter();
            } else if (event.key === "Backspace") {
                onDelete();
            } else {
                keys1.forEach((key) => {
                    if (event.key.toLowerCase() === key.toLowerCase()) {
                        onSelectLetter(key);
                    }
                });
                keys2.forEach((key) => {
                    if (event.key.toLowerCase() === key.toLowerCase()) {
                        onSelectLetter(key);
                    }
                });
                keys3.forEach((key) => {
                    if (event.key.toLowerCase() === key.toLowerCase()) {
                        onSelectLetter(key);
                    }
                });
            }
        },
        [onDelete, onEnter, onSelectLetter]
    );
    useEffect(() => {
        document.addEventListener("keydown", handleKeyboard);

        return () => {
            document.removeEventListener("keydown", handleKeyboard);
        };
    }, [handleKeyboard]);

    return (
        <div className="keyboard" onKeyDown={handleKeyboard}>
            <div className="keyboardLine">
                {keys1.map((key) => {
                    return <Key key={key} keyVal={key} disabled={!availableLetters.has(key)}/>;
                })}
            </div>
            <div className="keyboardLine">
                {keys2.map((key) => {
                    return <Key key={key} keyVal={key} disabled={!availableLetters.has(key)}/>;
                })}
            </div>
            <div className="keyboardLine">
                <Key keyVal={"ENTER"} bigKey/>
                {keys3.map((key) => {
                    return <Key key={key} keyVal={key} disabled={!availableLetters.has(key)}/>;
                })}
                <Key keyVal={"DELETE"} bigKey/>
            </div>
        </div>
    );
}

export default Keyboard;