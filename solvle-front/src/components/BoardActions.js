import React, {useContext, useState} from 'react';
import AppContext from "../contexts/contexts";
import {Button, Form, FormControl, InputGroup, Modal} from "react-bootstrap";
import Controls from "./Controls";
import SolveModal from "./SolveModal";

function BoardActions() {

    const {
        boardState,
        resetBoard,
        setAllUnavailable
    } = useContext(AppContext);

    const releaseFocus = (e) => {
        if (e && e.target) {
            e.target.blur();
        }
    }

    const clickReset = (e) => {
        resetBoard(boardState.settings.attempts, boardState.settings.wordLength);
        releaseFocus(e);
    }

    const excludeCurrent = (e) => {
        setAllUnavailable();
        releaseFocus(e);
    }


    return (
        <div>
            <Button title="clear all letters from the board" variant="dark" onClick={clickReset}>Reset Board</Button>
            <Button title="set all letters unavailable" variant="danger" onClick={excludeCurrent}>Exclude All</Button>
            <SolveModal />
        </div>
    );
}

export default BoardActions;