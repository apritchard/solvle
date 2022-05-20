import React, {useContext, useEffect, useState} from 'react';
import AppContext from "../contexts/contexts";
import OptionTab from "./OptionTab";
import {Spinner, Tab, Tabs} from "react-bootstrap";
import {generateConfigParams, generateRestrictionString} from "../functions/functions";
import RutTab from "./RutTab";

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
        setLoading(true);

        console.log("Building RestrictionString")
        console.log("Available letters: " + [...availableLetters]);

        let restrictionString = generateRestrictionString(availableLetters, knownLetters, unsureLetters);

        console.log("Fetching " + restrictionString + " with bias:" + boardState.settings.useBias + " partitioning:" + boardState.settings.usePartitioning);

        let configParams = generateConfigParams(boardState);

        fetch('/solvle/' + restrictionString + "?wordLength=" + boardState.settings.wordLength + "&wordList=" + dictionary + configParams)
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
                <Tab eventKey="viable" title="Pick👍" tabClassName="viableTab" tabAttrs={{title:"Words suggested based on how common their characters are among all the possible words. Click a word to add it to the board."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <OptionTab wordList={currentOptions.wordList} onSelectWord={onSelectWord}
                               heading={currentOptions.totalWords + " possible words"}/> }
                </Tab>
                <Tab eventKey="fishing" title="Fish🐟" tabClassName="fishingTab" tabAttrs={{title:"Words that maximize revealing new letters based on their frequency in the viable word set. Includes non-viable solutions."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <OptionTab wordList={currentOptions.fishingWords} onSelectWord={onSelectWord}
                               heading={"Fishing Words"}/> }
                </Tab>
                { boardState.settings.usePartitioning && currentOptions.bestWords !== null &&
                    <Tab eventKey="Remain" title="Cut✂" tabClassName="remTab" tabAttrs={{title:"Words that leave the fewest remaining choices."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <OptionTab wordList={currentOptions.bestWords} onSelectWord={onSelectWord}
                               heading={currentOptions.bestWords.length <= 0 ? "Too many viable words " : "Minimize Remaining"}/> }
                </Tab> }

                { (!boardState.settings.usePartitioning || currentOptions.bestWords === null) &&
                    <Tab eventKey="Remain" title="Cut✂" tabClassName="remTab">
                        <div>Enable Partition Calculation in the Settings menu to activate this tab.</div>
                    </Tab>

                }

                { boardState.settings.useRutBreaking && currentOptions.knownPositions !== null &&
                    <Tab eventKey="rutBreak" title="Rut⛏" tabClassName="rutab" tabAttrs={{title:"Word ruts are groups of viable words that share 3 or more letters. Watch out for ruts in hard mode to avoid getting trapped!"}}>
                        {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                        {!loading && <RutTab knownPositions={currentOptions.knownPositions} onSelectWord={onSelectWord}
                                                heading={currentOptions.knownPositions.length <= 0 ? "No ruts with " + boardState.settings.calculationConfig.rutBreakThreshold + " words found for position or too many viable words." : ""}/> }
                    </Tab> }
            </Tabs>
        </div>

    );
}

export default Options;