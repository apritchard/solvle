import React, {useContext, useEffect} from 'react';
import {AppContext} from "../App";

function Options(props) {

    const {
        currentOptions,
        setCurrentOptions,
        availableLetters,
        knownLetters,
        unsureLetters,
        onSelectWord,
        boardState,
        dictionary
    } =
        useContext(AppContext);

    useEffect(() => {
        console.log("Building WordleString")
        console.log("Available letters: " + [...availableLetters]);

        let wordleString = "";

        "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("").filter(letter => availableLetters.has(letter)).forEach(letter => {
            wordleString += letter;
            knownLetters.forEach((l, pos) => {
                if (l === letter) {
                    console.log("Known letter " + letter + " pos " + (pos+1));
                    wordleString += (pos +1);
                }
            });
            let hasUnsure = false;
            unsureLetters.forEach((letters, pos) => {
                if(letters.has(letter)) {
                    if(!hasUnsure) {
                        hasUnsure = true;
                        wordleString += "!";
                    }
                    console.log("unsure letter " + letter + " pos " + (pos+1));
                    wordleString += (pos+1);
                }
            });
        })

        console.log("Fetching " + wordleString);
        fetch('/solvle/' + wordleString + "?wordLength=" + boardState.settings.wordLength + "&wordleDict=" + dictionary)
            .then(res => res.json())
            .then((data) => {
                console.log(data);
                setCurrentOptions(data);
            });
    }, [setCurrentOptions, boardState.settings.wordLength, availableLetters, knownLetters, unsureLetters, dictionary]);

    return (

        <div>
            <div className="options">
                {currentOptions.totalWords + " " + boardState.settings.wordLength + "-letter words"}
                <ol>
                    {[...currentOptions.wordList].slice(0,100).map((item, index) => (
                        <li className="optionItem" key={item.word} value={index +1} onClick={() => onSelectWord(item.word.toUpperCase())}>{item.word + " (" + item.freqScore.toFixed(2) + ")"}</li>
                    ))}
                </ol>
                <div title="Words that maximize the unused letters, but are not required to use known letters">Fishing words:</div>
                <ol>
                    {[...currentOptions.fishingWords].slice(0, 100).map((item, index) => (
                        <li className="optionItem" key={"fish" + item.word} value={index+1} onClick={() => onSelectWord(item.word.toUpperCase())}>{item.word + " (" + item.freqScore.toFixed(2) + ")"}</li>
                    ))}
                </ol>
            </div>
        </div>

    );
}

export default Options;