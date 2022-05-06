import React, {useContext, useEffect, useState} from 'react';
import AppContext from "../contexts/contexts";
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
    } = useContext(AppContext);

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
        console.log("Fetching " + restrictionString + " with bias:" + boardState.settings.useBias + " partitioning:" + boardState.settings.usePartitioning);

        let biasParams = boardState.settings.useBias ?
            "&rightLocationMultiplier=" + boardState.settings.calculationConfig.rightLocationMultiplier +
            "&uniquenessMultiplier=" + boardState.settings.calculationConfig.uniquenessMultiplier +
            "&viableWordPreference=" + boardState.settings.calculationConfig.viableWordPreference
            : "&rightLocationMultiplier=0&uniquenessMultiplier=0&viableWordPreference=0";

        let partitionParams = boardState.settings.usePartitioning ?
            "&partitionThreshold=" + boardState.settings.calculationConfig.partitionThreshold
            : "&partitionThreshold=0";

        fetch('/solvle/' + restrictionString + "?wordLength=" + boardState.settings.wordLength + "&wordList=" + dictionary + biasParams + partitionParams)
            .then(res => res.json())
            .then((data) => {
                if(data.restrictionString !== restrictionString) {
                    console.log("Received old data, disregarding");
                    return;
                }
                console.log(data);
                setCurrentOptions(data);
                setLoading(false);
            });
    }, [setCurrentOptions, boardState.settings.wordLength, boardState.settings.useBias, boardState.settings.usePartitioning, boardState.shouldUpdate,
        availableLetters, knownLetters, unsureLetters, dictionary]);

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
                { boardState.settings.usePartitioning && currentOptions.bestWords !== null &&
                    <Tab eventKey="Remain" title="Remain" tabClassName="remTab" tabAttrs={{title:"Words that leave the fewest remaining choices."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <OptionTab wordList={currentOptions.bestWords} onSelectWord={onSelectWord}
                               heading={currentOptions.bestWords.length <= 0 ? "Too many viable words " : "Minimize Remaining"}/> }
                </Tab> }

                { (!boardState.settings.usePartitioning || currentOptions.bestWords === null) &&
                    <Tab eventKey="Remain" title="Remain" tabClassName="remTab">
                        <div>Enable Partitioning Calculation in the Settings menu to access this tab.</div>
                    </Tab>

                }
            </Tabs>
        </div>

    );
}

export default Options;