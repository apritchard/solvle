import React, {useContext, useEffect, useState} from 'react';
import {Button, Form, Modal} from "react-bootstrap";
import AppContext from "../contexts/contexts";

function AutoColor(props) {

    const {
        setSolverOpen,
        boardState,
        setAutoColorSolution
    } = useContext(AppContext);

    const [solution, setSolution] = useState("");

    const [valid, setValid] = useState(true);

    const [modalOpen, setModalOpen] = useState(false);

    const handleShow = (e) => {
        if (e && e.target) {
            e.target.blur();
        }
        setValid(true);
        setModalOpen(true);
        setSolverOpen(true);
    }
    const handleClose = (e) => {
        if(e != undefined) {
            e.preventDefault();
        }
        if(solution.trim().length == boardState.settings.wordLength || solution.length == 0) {
            setAutoColorSolution(solution.trim());
        } else {
            setValid(false);
        }
        setModalOpen(false);
        setSolverOpen(false);

    }

    const changeSolution = (e) => {
        setSolution(e.target.value);
    }

    console.log("Auto-color solution is valid or empty:" + valid);

    let buttonText = !valid ? "Invalid Solution" : boardState.settings.autoColorWord ? "Solution: " + boardState.settings.autoColorWord : "Set Solution";

    return (
        <span>
            <Button title="Automatically color letters based on a solution" variant="secondary"
                    onClick={handleShow}>{buttonText}</Button>

            <Modal className="autoColorModal" show={modalOpen} onHide={handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>Automatically Color Input</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>Entering a word below will cause Solvle to automatically color the letters of words you enter as if the specified word is the solution.</p>
                    <p>Clear the solution word to disable automatic coloring.</p>
                    <hr />
                    <Form onSubmit={handleClose}>
                        <Form.Group className="mb-3" controlId="formSolution">
                            <Form.Label>Solution</Form.Label>
                            <Form.Control value={solution} onChange={changeSolution} autoComplete="off" autoFocus
                                          type="text" placeholder="The answer to today's puzzle goes here"/>
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose}>
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>
        </span>
    );
}

export default AutoColor;