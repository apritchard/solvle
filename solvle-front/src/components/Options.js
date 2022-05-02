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
                if(data.restrictionString != restrictionString) {
                    console.log("Recieved old data, disregarding");
                    return;
                }
                console.log(data);
                setCurrentOptions(data);
                setLoading(false);
            });
    }, [setCurrentOptions, boardState.settings.wordLength, availableLetters, knownLetters, unsureLetters, dictionary]);

    return (

        <div className="options">
            <Tabs id="possible-word-tabs" className="flex-nowrap tabList">
                <Tab eventKey="viable" title="Viable" tabClassName="viableTab" tabAttrs={{title:"Words suggested based on how common their characters are among all the possible words. Click a word to add it to the board."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <OptionTab wordList={currentOptions.wordList} onSelectWord={onSelectWord}
                               heading={currentOptions.totalWords + " possible words"}/> }
                </Tab>
                <Tab eventKey="fishing" title="Fishing" tabClassName="fishingTab" tabAttrs={{title:"Words that maximize revealing new letters based on their frequency in the viable word set. Includes non-viable solutions."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <OptionTab wordList={currentOptions.fishingWords} onSelectWord={onSelectWord}
                               heading={"Fishing Words"}/> }
                </Tab>
                <Tab eventKey="Remain" title="Remain" tabClassName="remTab" tabAttrs={{title:"Words that leave the fewest remaining choices."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <OptionTab wordList={currentOptions.bestWords} onSelectWord={onSelectWord}
                               heading={currentOptions.bestWords.length <= 0 ? "Too many viable words " : "Minimize Remaining"}/> }
                </Tab>
            </Tabs>
        </div>

    );
}

export default Options;