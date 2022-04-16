import React, {useState} from 'react';
import {Alert, Button} from "react-bootstrap";

function SolvleAlert({heading, message, persist, persistMessage}) {
    const [show, setShow] = useState(true);

    if (show) {
        return (
            <Alert variant="info" onClose={() => setShow(false)} dismissible>
                <Alert.Heading>{heading} </Alert.Heading>
                <p className="instructions">
                    {message}
                </p>
            </Alert>
        );
    }
    if (persist) {
        return <Button onClick={() => setShow(true)}>{persistMessage}</Button>;
    }
}

export default SolvleAlert;