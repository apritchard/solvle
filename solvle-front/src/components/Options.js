import React, {useContext, useEffect} from 'react';
import {AppContext} from "../App";

function Options(props) {

    const {
        currentOptions,
        setCurrentOptions,
        availableLetters,
        knownLetters,
        unsureLetters,
        onSelectWord
    } =
        useContext(AppContext);

    useEffect(() => {
        console.log("Building WordleString")
        console.log("Available letters: " + [...availableLetters]);
        let wordleString = [...availableLetters].join("");

        knownLetters.forEach((letter, pos) => {
            if (letter !== "") {
                console.log("Known letter " + letter + " pos " + pos);
                wordleString += "" + letter + (pos + 1);
            }
        });

        unsureLetters.forEach((letter, pos) => {
            for (let i = 0; i < letter.size; i++) {
                console.log("unsure letters " + [...letter] + " pos " + pos);
                wordleString += "" + [...letter][i] + "!" + (pos + 1);
            }
        });

        if(availableLetters.size === 26 && wordleString.length === 26) {
            console.log("All words available, skipping request");
            return;
        }

        console.log("Fetching " + wordleString);
        fetch('/solvle/5/' + wordleString)
            .then(res => res.json())
            .then((data) => {
                console.log(data);
                setCurrentOptions(data.words);
            });
    }, [availableLetters, knownLetters, unsureLetters]);

    return (

        <div>
            <div className="options">
                <ol>
                    {[...currentOptions].slice(0,100).map((item, index) => (
                        <li className="optionItem" key={item.word.word} value={index +1} onClick={() => onSelectWord(item.word.word.toUpperCase())}>{item.word.word + " (" + item.freqScore.toFixed(2) + ")"}</li>
                    ))}
                </ol>
            </div>
        </div>

    );
}

export default Options;