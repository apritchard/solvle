import React, {useContext} from 'react';
import {Button} from 'react-bootstrap';
import {MDBRange, MDBSwitch, MDBTooltip} from 'mdb-react-ui-kit';
import AppContext from "../contexts/contexts";

function Controls() {
    const {
        boardState,
        setBoardState,
        dictionary,
        setDictionary,
        resetBoard,
    } = useContext(AppContext);


    // prevent buttons from accidentally activating a second time when users press ENTER
    const releaseFocus = (e) => {
        if (e && e.target) {
            e.target.blur();
        }
    }
    const increaseWordLength = (e) => {
        if (boardState.settings.wordLength > 8) {
            return;
        }
        resetBoard(boardState.settings.attempts, boardState.settings.wordLength + 1);
        releaseFocus(e);
    }
    const decreaseWordLength = (e) => {
        if (boardState.settings.wordLength < 3) {
            return;
        }
        resetBoard(boardState.settings.attempts, boardState.settings.wordLength - 1);
        releaseFocus(e);
    }

    const increaseAttempts = (e) => {
        resetBoard(boardState.settings.attempts + 1, boardState.settings.wordLength);
        releaseFocus(e);
    }

    const decreaseAttempts = (e) => {
        if (boardState.settings.attempts < 2) {
            return;
        }
        resetBoard(boardState.settings.attempts - 1, boardState.settings.wordLength);
        releaseFocus(e);
    }

    const toggleResetWords = (event) => {
        console.log("Setting")
        setDictionary(event.target.value);
    }

    const toggleBias = (e) => {
        localStorage.setItem("useBias", "" + !boardState.settings.useBias);
        setBoardState(prev => ({
            ...prev,
            settings: {
                ...prev.settings,
                useBias: !boardState.settings.useBias
            }
        }));
    }

    const togglePartitioning = (e) => {
        localStorage.setItem("usePartitioning", "" + !boardState.settings.usePartitioning);
        setBoardState(prev => ({
            ...prev,
            settings: {
                ...prev.settings,
                usePartitioning: !boardState.settings.usePartitioning
            }
        }));
    }

    const updateConfig = (e) => {
        localStorage.setItem(e.target.name, e.target.value);
        setBoardState(prev => ({
            ...prev,
            settings: {
                ...prev.settings,
                calculationConfig: {
                    ...prev.settings.calculationConfig,
                    [e.target.name]: e.target.value
                }
            }
        }));
    }

    const setConfig = (rightLocationMultiplier, uniquenessMultiplier, viableWordPreference, partitionThreshold) => {
        localStorage.setItem("rightLocationMultiplier", rightLocationMultiplier);
        localStorage.setItem("uniquenessMultiplier", uniquenessMultiplier);
        localStorage.setItem("viableWordPreference", viableWordPreference);
        localStorage.setItem("partitionThreshold", partitionThreshold);
        setBoardState(prev => ({
            ...prev,
            settings: {
                ...prev.settings,
                calculationConfig: {
                    ...prev.settings.calculationConfig,
                    rightLocationMultiplier: rightLocationMultiplier,
                    uniquenessMultiplier: uniquenessMultiplier,
                    viableWordPreference: viableWordPreference,
                    partitionThreshold: partitionThreshold
                }
            }
        }));

    }

    const setPreset = (e) => {
        switch(e.target.name) {
            case "OPTIMAL_MEAN":
                return setConfig(4, 9, .007, 50);
            case "LOWEST_MAX":
                return setConfig(1, 5, .01, 50);
            case "THREE_OR_LESS":
                return setConfig(4, 8, .001, 50);
            case "FOUR_OR_LESS":
                return setConfig(3, 10, .007, 50);
            case "TWO_OR_LESS":
                return setConfig(10, 3, .25, 10);
            default:
                console.log("Invalid preset: " + e.target.name);
        }
    }

    const biasHelpText = "Calculation Bias enables Solvle to prioritize words that match the" +
        " positions of letters. Customize the extent of this prioritization using the sliders below."
    const correctLocationHelpText = "Correct Location Multiplier increases the score for letters matched if they are in the correct position. Increasing" +
        " this value helps Solvle recommend words that are likely to reveal the most information about letter position. Setting this value higher than the number" +
        " of letters in the word can result in suggestions that prioritize position matching ahead of overall information gained, which can help you solve in fewer guesses" +
        " but may risk failure."
    const uniquenessHelpText = "Uniqueness Multiplier increases score for each letter matched if that letter only appears once in the word. This causes" +
        " Solvle to reveal new letters with a higher priority. It is recommended to set this multiplier higher than the correct location multiplier if you want to" +
        " achieve the best average performance, or lower than the location multiplier if you want the best chance of guessing a word quickly at the risk of failure."
    const viableWordPreferenceHelpText = "This setting adds a small bonus to a word's score if it is one of the words that is a viable solution. This property" +
        " increases the likelihood of solving the puzzle in 2 or 3 moves, but a larger bonus distorts the heuristics and results in a lower average performance or failure. " +
        "The best value in simulations was very tiny, just enough to act as a tie-breaker between words of equal score."
    const partitionHelpText = "Partitioning calculates which words remove the largest percentage of available words from the viable pool by testing every" +
        " candidate word against each viable solution and counting how many choices can be eliminated. In simulations, partitioning solutions don't perform" +
        " much better than heuristics until the viable word list is down to about 50, so a value of 50 or lower is recommended."

    const lowestAverageHelpText = "Produces the best average score (about 3.4825) and never fails a puzzle.";
    const lowestMaxHelpText = "Finishes all puzzles in 5 or fewer guesses, but worse average performance (about 3.593).";
    const most2HelpText = "Achieves a score of 2 on about 6.5% of puzzles (about twice as often as other strategies), but fails FOYER and ROGER. Average score 3.577.";
    const most3HelpText = "Maximizes scores of 3 or lower and does not fail, but scores 6 on more words. Averages 3.486."
    const most4HelpText = "Maximizes safely scoring 4 or less without failing. Average 3.489."


    return (
        <div className="controls">
            <div>
                <Button title="increase the maximum word length" variant="primary" onClick={increaseWordLength}>Word
                    Size +</Button>
                <Button title="decrease the maximum word length" variant="secondary" onClick={decreaseWordLength}>WordSize
                    -</Button>
                <Button title="increase the number of guesses allowed" variant="primary" onClick={increaseAttempts}>Guesses
                    +</Button>
                <Button title="decrease the number of guesses allowed" variant="secondary" onClick={decreaseAttempts}>Guesses
                    -</Button>
            </div>
            <div>
                <div> Word Length: {boardState.settings.wordLength} </div>
                <div> Number of Guesses: {boardState.settings.attempts} </div>
            </div>
            <hr/>
            <div onChange={toggleResetWords} className="wordLists">
                Word List:
                <span
                    title="2315 words: https://github.com/techtribeyt/Wordle/blob/main/wordle_answers.txt Defaults to Scrabble for words that are not 5-letters.">
                    <input id="simpleRadio" defaultChecked={dictionary === "simple"} type="radio" value="simple"
                           name="dict"/>
                    <label htmlFor="simpleRadio">Simple</label>
                </span>
                <span title="172820 words: https://github.com/dolph/dictionary/blob/master/enable1.txt">
                    <input id="bigRadio" defaultChecked={dictionary === "big"} type="radio" value="big" name="dict"/>
                    <label htmlFor="bigRadio">Scrabble</label>
                </span>
                <span title="370103 words: https://github.com/dwyl/english-words/blob/master/words_alpha.txt">
                    <input id="hugeRadio" defaultChecked={dictionary === "huge"} type="radio" value="huge" name="dict"/>
                    <label htmlFor="hugeRadio">Huge</label>
                </span>
            </div>
            <hr/>
            <div>
                <div title={biasHelpText} >
                    <MDBSwitch id='enableBiasSwitch' label="Enable Calculation Bias"
                               defaultChecked={boardState.settings.useBias} onChange={toggleBias}/>
                </div>
                <div title={partitionHelpText}>
                    <MDBSwitch id='enablePartitioningSwitch' label="Enable Partitioning Calculation"
                               defaultChecked={boardState.settings.usePartitioning} onChange={togglePartitioning}/>
                </div>
            </div>
            <div hidden={!boardState.settings.useBias}>
                <hr />
                Select a preset optimization or customize manually.
                <div>
                    <Button name="OPTIMAL_MEAN" title={lowestAverageHelpText} onClick={setPreset}>Lowest Average</Button>
                    <Button name="LOWEST_MAX" title={lowestMaxHelpText} onClick={setPreset} variant="secondary">Lowest Max</Button>
                    <Button name="TWO_OR_LESS" title={most2HelpText} onClick={setPreset}>Most 2</Button>
                    <Button name="THREE_OR_LESS" title={most3HelpText} onClick={setPreset} variant="secondary">Most 3</Button>
                    <Button name="FOUR_OR_LESS" title={most4HelpText} onClick={setPreset}>Most 4</Button>
                </div>
                <div title={correctLocationHelpText}>
                    <MDBRange
                        value={boardState.settings.calculationConfig.rightLocationMultiplier}
                        label='Correct Location Multiplier: recommended [3-5]'
                        name='rightLocationMultiplier'
                        min='1' max='20' step='0.5'
                        id='rightLocationRangeRange'
                        onChange={updateConfig}
                    />
                </div>
                <div title={uniquenessHelpText}>
                    <MDBRange
                        value={boardState.settings.calculationConfig.uniquenessMultiplier}
                        label='Uniqueness Multiplier: recommended [4-9]'
                        name='uniquenessMultiplier'
                        min='1' max='20' step='0.5'
                        id='uniquenessRange'
                        onChange={updateConfig}
                    />
                </div>
                <div title={viableWordPreferenceHelpText}>
                    <MDBRange
                        value={boardState.settings.calculationConfig.viableWordPreference}
                        label='Viable Word Preference: recommended [0.001-0.01]'
                        name='viableWordPreference'
                        min='0' max='0.5' step='0.001'
                        id='viableWordRange'
                        onChange={updateConfig}
                    />
                </div>
            </div>
            <div hidden={!boardState.settings.usePartitioning} title={partitionHelpText}>
                <hr/>
                <em>Note: experimental feature. Partition calculation can be slow and may impact Solvle's responsiveness.</em>
                <MDBRange
                    value={boardState.settings.calculationConfig.partitionThreshold}
                    label='Partition Threshold: recommended [10-50]'
                    name='partitionThreshold'
                    min='0' max='200' step='1'
                    id='partitionThresholdRange'
                    onChange={updateConfig}
                />
            </div>

        </div>
    );
}

export default Controls;