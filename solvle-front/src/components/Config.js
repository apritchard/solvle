import React, {useContext, useState} from "react";
import AppContext from "../contexts/contexts";
import {Button, Modal} from "react-bootstrap";
import Controls from "./Controls";

function Config(props) {

    const {
        boardState,
        setBoardState
    } = useContext(AppContext);

    const [config, setConfig] = React.useState(boardState.settings);

    const [show, setShow] = useState(false);

    const handleClose = () => {
        setShow(false);
        setBoardState(prev => ({
            ...prev,
            shouldUpdate: !boardState.shouldUpdate
        }));
    }
    const handleShow = () => setShow(true);

    return (
        <div>
            <Button variant="primary" onClick={handleShow}>
                <span>&#9881;</span>
            </Button>

            <Modal show={show} onHide={handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>Solvle Configuration</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Controls config={config} setConfig={setConfig} />
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="primary" onClick={handleClose}>
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}

export default Config;