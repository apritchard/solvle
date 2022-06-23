import React, {useContext, useEffect, useState} from 'react';
import AppContext from "../contexts/contexts";
import {Spinner, Tab, Tabs} from "react-bootstrap";
import {generateAnagramString} from "../functions/functions";
import AnagramOptionTab from "./AnagramOptionTab";

function AnagramOptions(props) {

    const {
        boardState
    } = useContext(AppContext);

    const [loading, setLoading] = useState(true);

    const [anagrams, setAnagrams] = useState(new Map())

    useEffect(() => {
        let anagramString = generateAnagramString(boardState.board);
        if(anagramString.length < 3) {
            console.log("String too short to find anagrams");
            return;
        }
        setLoading(true);

        console.log("Fetching anagrams for" + anagramString);

        fetch('/solvescape/' + anagramString)
            .then(res => {
                if (res.ok) {
                    return res.json()
                }
                throw new Error(res.statusMessage);
            })
            .then((data) => {
                console.log("options received:")
                console.log(data);
                setAnagrams(data);
                setLoading(false);
            }).catch((e) => {
                console.log("Error retrieving anagrams for " + anagramString);
                setAnagrams(new Map());
                setLoading(false);
        });
    }, [boardState.board]);

    return (

        <div className="options">
            <Tabs id="possible-word-tabs" className="flex-nowrap tabList">
                <Tab eventKey="viable" title="Anagrams🥧" tabClassName="anagramTab" tabAttrs={{title:"Anagrams of your selected letters."}}>
                    {loading && <div>Loading...<Spinner animation="border" role="status" /> </div>}
                    {!loading && <AnagramOptionTab wordMap={anagrams} heading={"Anagrams"}/> }
                </Tab>
            </Tabs>
        </div>

    );
}

export default AnagramOptions;