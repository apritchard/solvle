import React from 'react';

function RutTab({knownPositions, onSelectWord, heading}) {

    console.log(knownPositions);

    let wordListOutput = knownPositions != null && knownPositions.length > 0 ?  knownPositions.map(kp => <div>
        <div className="rutHeader" title="This is a group of viable words that share 3 or more letters.">
            <h5>{kp.position.toUpperCase()}</h5>
            {[...kp.words].join(', ')}
        </div>
        <hr/>
        <div hidden={kp.recommendations === null || kp.recommendations.length == 0}
             title="These are words that will eliminate the most possibilities within this word rut, but they may not be the best words for this position overall. Use with discretion.">
            <h6>Recommendations:</h6>
            <ol>
                {[...kp.recommendations].map((item, index) => (
                    <li className="optionItem" key={item.word} value={index + 1}
                        onClick={() => onSelectWord(item.word.toUpperCase())}>{item.word + " (" + (item.freqScore * 100).toFixed(0) + "%)"}</li>
                ))}
            </ol>
            <hr/>
        </div>
    </div>): "";

    return (
        <div>
            <div>{heading}</div>
            {wordListOutput}
        </div>
    );
}

export default RutTab;