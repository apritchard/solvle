import "./App.css";
import Board from "./components/Board";
import Keyboard from "./components/Keyboard";
import React, {useState, createContext, useEffect, useRef} from "react";
import Options from "./components/Options";
import WordleString from "./components/WordleString";

export const AppContext = createContext();

function App() {
    const [wordleString, setWordleString] = useState("a");
    const [availableLetters, setAvailableLetters] = useState(new Set("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("")));
    const [knownLetters, setKnownLetters] = useState( () => {
        let ret = new Map();
        for(let i = 0; i < 5; i++) {
            ret.set(i, "");
        }
        return ret;
    });
    const [unsureLetters, setUnsureLetters] = useState( () => {
        let ret = new Map();
        for(let i = 0; i < 5; i++) {
            ret.set(i, new Set());
        }
        return ret;
    });
    const [currentOptions, setCurrentOptions] = useState(new Set());

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
        setUnsureLetters(prev =>  {
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

    const updateOptions = () => {
        console.log("Fetching " + wordleString);
        fetch('/solvle/' + wordleString)
            .then(res => res.json())
            .then((data) => {
                console.log(data);
                setCurrentOptions(data);
            });
    }

    const buildWordleString = () => {
        console.log("Building WordleString")
        console.log("Available letters: " + [...availableLetters]);
        let tempString = [...availableLetters].join("");

        knownLetters.forEach( (letter, pos) => {
            if(letter !== "") {
                console.log("Known letter " + letter + " pos " + pos);
                tempString += "" + letter + (pos+1);
            }
        });

        unsureLetters.forEach( (letter, pos) => {
            for(let i = 0; i < letter.size; i++) {
                console.log("unsure letters " + [...letter] + " pos " + pos);
                tempString += "" + [...letter][i] + "!" + (pos+1);
            }
        });
        console.log(tempString);
        return tempString;
    }


  const [board, setBoard] = useState([
      ["", "", "", "", ""],
      ["", "", "", "", ""],
      ["", "", "", "", ""],
      ["", "", "", "", ""],
      ["", "", "", "", ""],
      ["", "", "", "", ""],
  ]);
  const [currAttempt, setCurrAttempt] = useState({ attempt: 0, letter: 0 });

  const onEnter = () => {
    if (currAttempt.letter !== 5) return;

    let currWord = "";
    for (let i = 0; i < 5; i++) {
      currWord += board[currAttempt.attempt][i];
    }
    setCurrAttempt({ attempt: currAttempt.attempt + 1, letter: 0 });
  };

  const onDelete = () => {
    if (currAttempt.letter === 0) return;
    const newBoard = [...board];
    newBoard[currAttempt.attempt][currAttempt.letter - 1] = "";
    setBoard(newBoard);
    setCurrAttempt({ ...currAttempt, letter: currAttempt.letter - 1 });
  };

  const onSelectLetter = (key) => {
    if (currAttempt.letter > 4) return;
    const newBoard = [...board];
    newBoard[currAttempt.attempt][currAttempt.letter] = key;
    setBoard(newBoard);
    setCurrAttempt({
      attempt: currAttempt.attempt,
      letter: currAttempt.letter + 1,
    });
  };

  return (
      <div className="App">
        <nav>
          <h1>Solvle</h1>
        </nav>
        <AppContext.Provider
            value={{
              board,
              setBoard,
              currAttempt,
              setCurrAttempt,
              onSelectLetter,
              onDelete,
              onEnter,
                availableLetters,
                knownLetters,
                unsureLetters,
                addKnownLetter,
                removeKnownLetter,
                addUnsureLetter,
                removeUnsureLetter,
                addAvailableLetter,
                removeAvailableLetter,
                currentOptions,
                setCurrentOptions,
                wordleString,
                setWordleString
            }}
        >
          <div className="game">
              <WordleString />
              <div class="parent">
            <Board />
              <Options />
              </div>
            <Keyboard />
          </div>
        </AppContext.Provider>
      </div>
  );
}

export default App;