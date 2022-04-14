/**
 * Wordle base started from the Wordle-Clone-React tutorial, available on github:
 * https://github.com/machadop1407/Wordle-Clone-React
 */

import "./App.css";
import Board from "./components/Board";
import Keyboard from "./components/Keyboard";
import React, {useState, createContext} from "react";
import Options from "./components/Options";
import Controls from "./components/Controls";

export const AppContext = createContext();

function App() {

    const initialBoard = (rows, width) => {
        let retArray = [];
        for (let i = 0; i < rows; i++) {
            retArray[i] = [];
            for (let j = 0; j < width; j++) {
                retArray[i][j] = "";
            }
        }
        return retArray;
    }

    const initialBoardState = (rows, width) => {
        return {
            board: initialBoard(rows, width),
            currAttempt: {
                attempt: 0,
                letter: 0
            },
            settings: {
                wordLength: width,
                attempts: rows,
                results: 50
            }
        }
    }

    const [boardState, setBoardState] = useState(initialBoardState(6, 5));

    const initialAvailableLetters = () => {
        return new Set("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""));
    }

    const initialKnownLetters = (width) => {
        let ret = new Map();
        for (let i = 0; i < width; i++) {
            ret.set(i, "");
        }
        return ret;
    }

    const initialUnsureLetters = (width) => {
        let ret = new Map();
        for (let i = 0; i < width; i++) {
            ret.set(i, new Set());
        }
        return ret;
    }

    const [availableLetters, setAvailableLetters] = useState(initialAvailableLetters());
    const [knownLetters, setKnownLetters] = useState(initialKnownLetters(boardState.settings.wordLength));
    const [unsureLetters, setUnsureLetters] = useState(initialUnsureLetters(boardState.settings.wordLength));
    const [currentOptions, setCurrentOptions] = useState(new Set());
    const [dictionary, setDictionary] = useState("default");

    const resetBoard = (rows, width) => {
        setBoardState(initialBoardState(rows, width));
        setAvailableLetters(initialAvailableLetters());
        setUnsureLetters(initialUnsureLetters(width))
        setKnownLetters(initialKnownLetters(width));
        setCurrentOptions(new Set());
    }

    const addKnownLetter = (pos, letter) => {
        setKnownLetters(prev => new Map(prev.set(pos, letter)));
    }

    const removeKnownLetter = (pos) => {
        setKnownLetters(prev => new Map(prev.set(pos, "")));
    }

    const addUnsureLetter = (pos, letter) => {
        setUnsureLetters(prev => new Map(prev.set(pos, prev.get(pos).add(letter))));
    }

    const removeUnsureLetter = (pos, letter) => {
        setUnsureLetters(prev => {
            prev.get(pos).delete(letter);
            return new Map(unsureLetters.set(pos, unsureLetters.get(pos)));
        });
    }

    const addAvailableLetter = (letter) => {
        setAvailableLetters(prev => new Set([...prev]).add(letter));
    }

    const removeAvailableLetter = (letter) => {
        setAvailableLetters(prev => {
            prev.delete(letter);
            return new Set([...prev]);
        });
    }

    const onEnter = () => {
        if (boardState.currAttempt.letter !== boardState.settings.wordLength) {
            return;
        }

        console.log("Updating board state");
        setBoardState(prev => ({
            ...prev,
            board: prev.board,
            currAttempt: {
                attempt: boardState.currAttempt.attempt + 1,
                letter: 0
            }
        }));
    };

    const onDelete = () => {
        if (boardState.currAttempt.letter === 0) {
            return;
        }
        const newBoard = [...boardState.board];
        newBoard[boardState.currAttempt.attempt][boardState.currAttempt.letter - 1] = "";
        setBoardState(prev => ({
            ...prev,
            board: newBoard,
            currAttempt: {
                attempt: boardState.currAttempt.attempt,
                letter: boardState.currAttempt.letter -1
            }
        }));
    };

    const onSelectLetter = (key) => {
        if (boardState.currAttempt.letter >= boardState.settings.wordLength ) {
            return;
        }
        const newBoard = [...boardState.board];
        newBoard[boardState.currAttempt.attempt][boardState.currAttempt.letter] = key;
        setBoardState(prev => ({
            ...prev,
            board: newBoard,
            currAttempt: {
                attempt: boardState.currAttempt.attempt,
                letter: boardState.currAttempt.letter +1
            }
        }));
    };

    const onSelectWord = (word) => {
        console.log("Setting " + word + word.length + " " + boardState.currAttempt.attempt + boardState.currAttempt.letter);
        if(boardState.currAttempt.letter !== 0) {
            return;
        }
        const newBoard = [...boardState.board];
        for(let i = 0; i < word.length; i++) {
            newBoard[boardState.currAttempt.attempt][i] = word[i];
        }
        setBoardState(prev => ({
            ...prev,
            board: newBoard,
            currAttempt: {
                attempt: boardState.currAttempt.attempt,
                letter: word.length
            }
        }));
    }

    return (
        <div className="App">
            <nav>
                <h1>Solvle</h1>
            </nav>
            <AppContext.Provider
                value={{
                    boardState,
                    setBoardState,
                    currentOptions,
                    setCurrentOptions,
                    availableLetters,
                    knownLetters,
                    unsureLetters,
                    dictionary,
                    setDictionary,
                    onSelectLetter,
                    onDelete,
                    onEnter,
                    addKnownLetter,
                    removeKnownLetter,
                    addUnsureLetter,
                    removeUnsureLetter,
                    addAvailableLetter,
                    removeAvailableLetter,
                    onSelectWord,
                    resetBoard
                }}
            >
                <div className="game">
                    <Controls />
                    <div className="parent">
                        <Board/>
                        <Options/>
                    </div>
                    <Keyboard/>
                </div>
            </AppContext.Provider>
        </div>
    );
}

export default App;