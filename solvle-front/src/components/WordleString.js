import React, {useContext, useEffect} from 'react';
import {AppContext} from "../App";

function WordleString(props) {
    const {
        availableLetters,
        unsureLetters,
        knownLetters, setWordleString, wordleString } =
        useContext(AppContext);

    useEffect( () => {
            console.log("Building WordleString")
            console.log("Available letters: " + [...availableLetters]);
            let tempString = [...availableLetters].join("");

            knownLetters.forEach( (letter, pos) => {
                if(letter !== "") {
                    console.log("Known letter " + letter + " pos " + pos);
                    tempString += "" + letter + (pos+1);
                }
            });

            unsureLetters.forEach( (letter, pos) => {
                for(let i = 0; i < letter.size; i++) {
                    console.log("unsure letters " + [...letter] + " pos " + pos);
                    tempString += "" + [...letter][i] + "!" + (pos+1);
                }
            });
            console.log(tempString);
            setWordleString(tempString);
        },
        [availableLetters, knownLetters, unsureLetters])
    return (
        <div>{wordleString}</div>
    );
}

export default WordleString;