import React, {useContext, useEffect, useState} from 'react';
import {AppContext} from "../App";
import OptionTab from "./OptionTab";
import {Spinner, Tab, Tabs} from "react-bootstrap";

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

    const [loading, setLoading] = useState(true);

    useEffect(() => {
        console.log("Building RestrictionString")
        console.log("Available letters: " + [...availableLetters]);

        let restrictionString = "";

        "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("").filter(letter => availableLetters.has(letter)).forEach(letter => {
            restrictionString += letter;
            knownLetters.forEach((l, pos) => {
                if (l === letter) {
                    console.log("Known letter " + letter + " pos " + (pos + 1));
                    restrictionString += (pos + 1);
                }
            });
            let hasUnsure = false;
            unsureLetters.forEach((letters, pos) => {
                if (letters.has(letter)) {
                    if (!hasUnsure) {
                        hasUnsure = true;
                        restrictionString += "!";
                    }
                    console.log("unsure letter " + letter + " pos " + (pos + 1));
                    restrictionString += (pos + 1);
                }
            });
        })

        setLoading(true);
        console.log("Fetching " + restrictionString);
        fetch('/solvle/' + restrictionString + "?wordLength=" + boardState.settings.wordLength + "&wordList=" + dictionary)
            .then(res => res.json())
            .then((data) => {
                console.log(data);
                setCurrentOptions(data);
                setLoading(false);
            });
    }, [setCurrentOptions, boardState.settings.wordLength, availableLetters, knownLetters, unsureLetters, dictionary]);

    return (

        <div className="options">
            <Tabs id="possible-word-tabs" className="flex-nowrap">
                <Tab eventKey="viable" title="Viable" tabClassName="viableTab" tabAttrs={{title:"Words suggested based on how common their characters are among all the possible words. Click a word to add it to the board."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <OptionTab wordList={currentOptions.wordList} onSelectWord={onSelectWord}
                               heading={currentOptions.totalWords + " possible words"}/> }
                </Tab>
                <Tab eventKey="fishing" title="Fishing" tabClassName="fishingTab" tabAttrs={{title:"Words that maximize the commonly used letters in the possible word set, but de-prioritize known letters."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <OptionTab wordList={currentOptions.fishingWords} onSelectWord={onSelectWord}
                               heading={"Fishing Words"}/> }
                </Tab>
            </Tabs>
        </div>

    );
}

export default Options;