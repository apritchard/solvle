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

    const updateSetting = (e) => {
        localStorage.setItem(e.target.name, e.target.checked);
        setBoardState(prev => ({
            ...prev,
            settings: {
                ...prev.settings,
                [e.target.name]: e.target.checked
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

    const setConfig = (rightLocationMultiplier, uniquenessMultiplier, viableWordPreference, partitionThreshold, locationAdjustmentScale, uniqueAdjustmentScale, viableWordAdjustmentScale, vowelMultiplier) => {
        localStorage.setItem("rightLocationMultiplier", rightLocationMultiplier);
        localStorage.setItem("uniquenessMultiplier", uniquenessMultiplier);
        localStorage.setItem("viableWordPreference", viableWordPreference);
        localStorage.setItem("partitionThreshold", partitionThreshold);
        localStorage.setItem("locationAdjustmentScale", locationAdjustmentScale);
        localStorage.setItem("uniqueAdjustmentScale", uniqueAdjustmentScale);
        localStorage.setItem("viableWordAdjustmentScale", viableWordAdjustmentScale);
        localStorage.setItem("vowelMultiplier", vowelMultiplier);
        setBoardState(prev => ({
            ...prev,
            settings: {
                ...prev.settings,
                calculationConfig: {
                    ...prev.settings.calculationConfig,
                    rightLocationMultiplier: rightLocationMultiplier,
                    uniquenessMultiplier: uniquenessMultiplier,
                    viableWordPreference: viableWordPreference,
                    partitionThreshold: partitionThreshold,
                    locationAdjustmentScale: locationAdjustmentScale,
                    uniqueAdjustmentScale: uniqueAdjustmentScale,
                    viableWordAdjustmentScale: viableWordAdjustmentScale,
                    vowelMultiplier: vowelMultiplier
                }
            }
        }));

    }

    const setPreset = (e) => {
        switch(e.target.name) {
            case "OPTIMAL_MEAN":
                return setConfig(3, 8, .007, 110, 1, 0, 0, 0.6);
            case "LOWEST_MAX":
                return setConfig(5, 5, .01, 100, 1, 0, 0, 0.6);
            case "THREE_OR_LESS":
                return setConfig(4, 8, .001, 50, 0.6, 0, 1, 0.9);
            case "FOUR_OR_LESS":
                return setConfig(3, 9, .007, 100, 0.6, 1, 0, 0.6);
            case "TWO_OR_LESS":
                return setConfig(3, 4, .4, 50, 0, 0, 0, 0.9);
            default:
                console.log("Invalid preset: " + e.target.name);
        }
    }

    const hardModeHelpText = "Limit word suggestions to words that are available in hard mode."
    const rateEnteredWordsHelpText = "Show fishing score and average words remaining for each word you enter. Calculates when you press ENTER using the currently selected letters, so" +
        " it will only be accurate if you mark the state of your previous words, but not your current word, before pressing enter.";
    const biasHelpText = "Calculation Bias enables Solvle to prioritize words that match the" +
        " positions of letters. Customize the extent of this prioritization using the sliders below.";
    const partitionHelpText = "Partitioning calculates which words remove the largest percentage of available words from the viable pool by testing every" +
        " candidate word against each viable solution and counting how many choices can be eliminated. In simulations, partitioning solutions don't perform" +
        " much better than heuristics until the viable word list is down to about 50, so a value of 50 or lower is recommended.";
    const useRutBreakingHelpText = "Identify groups of words with 3 or more shared letters that could match the current position. For example, if your current" +
        " position matched [EIGHT, FIGHT, LIGHT, NIGHT, SIGHT, TIGHT], this group of words would form the _IGHT rut.";

    const fineTuningHelpText = "Enable options to further customize the behavior of the calculation bias.";
    const correctLocationHelpText = "Correct Location Multiplier increases the score for letters matched if they are in the correct position. Increasing" +
        " this value helps Solvle recommend words that are likely to reveal the most information about letter position. Setting this value higher than the number" +
        " of letters in the word can result in suggestions that prioritize position matching ahead of overall information gained, which can help you solve in fewer guesses" +
        " but may risk failure.";
    const uniquenessHelpText = "Uniqueness Multiplier increases score for each letter matched if that letter only appears once in the word. This causes" +
        " Solvle to reveal new letters with a higher priority. It is recommended to set this multiplier higher than the correct location multiplier if you want to" +
        " achieve the best average performance, or lower than the location multiplier if you want the best chance of guessing a word quickly at the risk of failure.";
    const viableWordPreferenceHelpText = "This setting adds a small bonus to a word's score if it is one of the words that is a viable solution. This property" +
        " increases the likelihood of solving the puzzle in 2 or 3 moves, but a larger bonus distorts the heuristics and results in a lower average performance or failure. " +
        "The best value in simulations was very tiny, just enough to act as a tie-breaker between words of equal score.";
    const vowelMultiplierHelpText = "Multiply the score awarded for matching vowels in words by a fixed amount. Usually a slight reduction in vowel score improves average" +
        " performance by helping Solvle eliminate more consonants early, avoiding problems later.";
    const locationScaleHelpText = "Scales down the location multiplier the more positions you already know. This can help Solvle avoid over-valuing letters that are in a" +
        " correct position when you already know most of the positions in a word.";
    const uniquenessScaleHelpText = "Scales down the uniqueness multiplier the fewer letters remain. This can help Solvle avoid over-valuing learning new letters if you've" +
        " already eliminated many letters from consideration.";
    const rutBreakingThresholdHelpText = "Specifies the minimum number of words with 3 or more of the same shared letters to be considered a rut."
    const rutBreakMultiplierHelpText = "Adds a bonus to calculation bias for letters that can help resolve words in ruts. This can cause Solvle to adjust its fishing word recommendations."

    const lowestAverageHelpText = "Produces the best average score (about 3.45745) and never fails a puzzle.";
    const lowestMaxHelpText = "Finishes all puzzles in 5 or fewer guesses, but worse average performance (about 3.54816).";
    const most2HelpText = "Achieves a score of 2 on about 6.5% of puzzles (about twice as often as other strategies), but can fail if used past word 2. Average score 3.55982.";
    const most3HelpText = "Maximizes scores of 3 or lower and does not fail, but scores 6 on more words. Averages 3.46781.";
    const most4HelpText = "Maximizes safely scoring 4 or less without failing. Average 3.45831.";


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
                <div title={hardModeHelpText} >
                    <MDBSwitch id='hardModeSwitch' label="Hard Mode" name="hardMode"
                               defaultChecked={boardState.settings.hardMode} onChange={updateSetting}/>
                </div>
                <div title={rateEnteredWordsHelpText}>
                    <MDBSwitch id='enableWordRatingSwitch' label="Rate Words As You Enter" name="rateEnteredWords"
                               defaultChecked={boardState.settings.rateEnteredWords} onChange={updateSetting}/>
                </div>
                <div title={biasHelpText} >
                    <MDBSwitch id='enableBiasSwitch' label="Enable Calculation Bias" name="useBias"
                               defaultChecked={boardState.settings.useBias} onChange={updateSetting}/>
                </div>
                <div title={partitionHelpText}>
                    <MDBSwitch id='enablePartitioningSwitch' label="Enable Partitioning Calculation" name="usePartitioning"
                               defaultChecked={boardState.settings.usePartitioning} onChange={updateSetting}/>
                </div>
                <div title={useRutBreakingHelpText}>
                    <MDBSwitch id='useRutBreakingSwitch' label="Enable Rut Identification" name="useRutBreaking"
                               defaultChecked={boardState.settings.useRutBreaking} onChange={updateSetting}/>
                </div>
            </div>
            <div hidden={!boardState.settings.useBias}>
                <hr />
                <h6 title={biasHelpText}>Calculation Bias Settings</h6>
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
                <div title={fineTuningHelpText} >
                    <MDBSwitch id='fineTuningSwitch' label="Enable Fine Tuning Options" name="useFineTuning"
                               defaultChecked={boardState.settings.useFineTuning} onChange={updateSetting}/>
                </div>
                <div hidden={!boardState.settings.useFineTuning}>
                    <hr />
                    <h6>Fine Tuning Options</h6>
                    <div title={vowelMultiplierHelpText}>
                        <MDBRange
                            value={boardState.settings.calculationConfig.vowelMultiplier}
                            label='Vowel Multiplier: recommended [0.6-0.9]'
                            name='vowelMultiplier'
                            min='0' max='2' step='0.1'
                            id='vowelMultiplierRange'
                            onChange={updateConfig}
                        />
                    </div>
                    <div title={locationScaleHelpText}>
                        <MDBRange
                            value={boardState.settings.calculationConfig.locationAdjustmentScale}
                            label='Adjust Location Scale: recommended [0.6-1]'
                            name='locationAdjustmentScale'
                            min='0' max='1' step='0.1'
                            id='locationAdjustmentRange'
                            onChange={updateConfig}
                        />
                    </div>
                    <div title={uniquenessScaleHelpText}>
                        <MDBRange
                            value={boardState.settings.calculationConfig.uniqueAdjustmentScale}
                            label='Adjust Uniqueness Scale: recommended [0.0-0.2]'
                            name='uniqueAdjustmentScale'
                            min='0' max='1' step='0.1'
                            id='uniqueAdjustmentRange'
                            onChange={updateConfig}
                        />
                    </div>
                </div>

            </div>
            <div hidden={!boardState.settings.usePartitioning} title={partitionHelpText}>
                <hr/>
                <h6>Partitioning</h6>
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
            <div hidden={!boardState.settings.useRutBreaking} >
                <hr/>
                <h6 title={useRutBreakingHelpText}>Rut Identification</h6>
                <div title={rutBreakingThresholdHelpText}>
                    <MDBRange
                        value={boardState.settings.calculationConfig.rutBreakThreshold}
                        label='Minimum rut size: recommended [8-15]'
                        name='rutBreakThreshold'
                        min='6' max='30' step='1'
                        id='rutBreakThresholdRange'
                        onChange={updateConfig}
                    />
                </div>
                <div title={rutBreakMultiplierHelpText}>
                    <MDBRange
                        value={boardState.settings.calculationConfig.rutBreakMultiplier}
                        label='Matching Letter Bonus: recommended [0-1]'
                        name='rutBreakMultiplier'
                        min='0' max='10' step='.1'
                        id='rutBreakMultiplierRange'
                        onChange={updateConfig}
                    />
                </div>
            </div>
        </div>
    );
}

export default Controls;