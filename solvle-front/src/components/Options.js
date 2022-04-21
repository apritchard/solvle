import React, {useContext, useEffect, useState} from 'react';
import {AppContext} from "../App";
import OptionTab from "./OptionTab";
import {Tab, Tabs} from "react-bootstrap";

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
                    console.log("Known letter " + letter + " pos " + (pos + 1));
                    wordleString += (pos + 1);
                }
            });
            let hasUnsure = false;
            unsureLetters.forEach((letters, pos) => {
                if (letters.has(letter)) {
                    if (!hasUnsure) {
                        hasUnsure = true;
                        wordleString += "!";
                    }
                    console.log("unsure letter " + letter + " pos " + (pos + 1));
                    wordleString += (pos + 1);
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

        <div className="options">
            <Tabs id="possible-word-tabs" className="flex-nowrap">
                <Tab eventKey="viable" title="Viable" tabClassName="viableTab" tabAttrs={{title:"Words suggested based on how common their characters are among all the possible words. Click a word to add it to the board."}}>
                    <OptionTab wordList={currentOptions.wordList} onSelectWord={onSelectWord}
                               heading={currentOptions.totalWords + " possible words"}/>
                </Tab>
                <Tab eventKey="fishing" title="Fishing" tabClassName="fishingTab" tabAttrs={{title:"Words that maximize the commonly used letters in the possible word set, but de-prioritize known letters."}}>
                    <OptionTab wordList={currentOptions.fishingWords} onSelectWord={onSelectWord}
                               heading={"Fishing Words"}/>
                </Tab>
            </Tabs>
        </div>

    );
}

export default Options;