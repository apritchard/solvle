import React, {useCallback, useContext, useEffect} from 'react';
import {AppContext} from "../App";
import WordleString from "./WordleString";

function Options(props) {

    const { currentOptions, setCurrentOptions, wordleString } =
        useContext(AppContext);


    // useEffect(() => {
    //     console.log("Building WordleString")
    //     console.log("Available letters: " + [...availableLetters]);
    //     tempString = [...availableLetters].join("");
    //
    //     knownLetters.forEach( (letter, pos) => {
    //         if(letter !== "") {
    //             console.log("Known letter " + letter + " pos " + pos);
    //             tempString += "" + letter + (pos+1);
    //         }
    //     });
    //
    //     unsureLetters.forEach( (letter, pos) => {
    //         for(let i = 0; i < letter.size; i++) {
    //             console.log("unsure letters " + [...letter] + " pos " + pos);
    //             tempString += "" + [...letter][i] + "!" + (pos+1);
    //         }
    //     });
    //     console.log(tempString);
    //
    //         console.log("Fetching " + tempString);
    //         fetch('/solvle/' + tempString)
    //             .then(res => res.json())
    //             .then((data) => {
    //                 console.log(data);
    //                 options = data;
    //             });
    //
    // }, [availableLetters, unsureLetters, knownLetters]);

    useEffect(() => {
        console.log("Fetching " + wordleString);
        fetch('/solvle/' + wordleString)
            .then(res => res.json())
            .then((data) => {
                console.log(data);
                setCurrentOptions(data);
            });
    }, [wordleString]);
    //
    // const handleKeyboard = useCallback(
    //     (event) => {
    //         if (event.key === "Enter") {
    //             console.log("Fetching " + wordleString);
    //             fetch('/solvle/' + wordleString)
    //                 .then(res => res.json())
    //                 .then((data) => {
    //                     console.log(data);
    //                     setCurrentOptions(data);
    //                 });
    //         }
    //     },
    //     [wordleString]
    // );
    // useEffect(() => {
    //     document.addEventListener("keydown", handleKeyboard);
    //
    //     return () => {
    //         document.removeEventListener("keydown", handleKeyboard);
    //     };
    // }, [handleKeyboard]);

    return (

        <div>
            <div class="options">
                <ol>
                { [...currentOptions].map((item, index) => (
                    <li>{item}</li>
                ))}
                </ol>
            </div>
        </div>

    );
}

export default Options;