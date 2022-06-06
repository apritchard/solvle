import React, {useContext, useEffect, useState} from 'react';
import {Button, Form, Modal} from "react-bootstrap";
import AppContext from "../contexts/contexts";
import {generateConfigParams} from "../functions/functions";

function SolveModal(props) {

    const {
        boardState,
        solverOpen,
        setSolverOpen
    } = useContext(AppContext);

    const [firstWord, setFirstWord] = useState("");
    const [solution, setSolution] = useState("");
    const [guesses, setGuesses] = useState([]);


    const [modalOpen, setModalOpen] = useState(false);

    const handleShow = () => {
        setModalOpen(true);
        setSolverOpen(true);
    }
    const handleClose = () => {
        setModalOpen(false);
        setSolverOpen(false);
    }

    const changeSolution = (e) => {
        setSolution(e.target.value);
    }
    const changeFirstWord = (e) => {
        setFirstWord(e.target.value);
    }

    const solvePuzzle = (e) => {
        e.preventDefault();
        let configParams = generateConfigParams(boardState);

        fetch('/solvle/solve/' + solution + "?firstWord=" + firstWord + configParams)
            .then(res => res.json())
            .then((data) => {
                console.log(data);
                setGuesses(data);
            });
    }

    return (
        <span>
            <Button title="Show Solvle's solution for a specific word" variant="info"
                    onClick={handleShow}>Solve Word</Button>

            <Modal className="solveModal" show={modalOpen} onHide={handleClose} >
                <Modal.Header closeButton>
                    <Modal.Title>Solve Word</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>Put Solvle to the test!</p>
                    <p>You provide the solution and Solvle will try to guess it using the configuration you've provided in the setup menu.</p>
                    <p>Enter your own starting word if you want to see how Solvle does from your first choice.</p>
                    <hr />
                    <Form onSubmit={solvePuzzle}>
                        <Form.Group className="mb-3" controlId="formSolution">
                            <Form.Label>Solution</Form.Label>
                            <Form.Control value={solution} onChange={changeSolution} autoComplete="off" autoFocus
                                          type="text" placeholder="The answer to today's puzzle goes here"/>
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="formStartingWord">
                            <Form.Label>Starting Word (optional)</Form.Label>
                            <Form.Control value={firstWord} onChange={changeFirstWord} autoComplete="off"
                                          type="text" placeholder="Starting Word"/>
                        </Form.Group>
                        <Button variant="primary" type="submit">
                            Solve!
                        </Button>
                    </Form>

                    Solvle's Guesses:
                    <ol>
                      {guesses.map((item, index) => (
                          <li className="guess" key={item} value={index + 1}>{item.toUpperCase()}</li>
                      ))}
                    </ol>

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

export default SolveModal;