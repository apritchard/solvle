import React from 'react';

function OptionTab({wordList, onSelectWord, heading}) {
    return (
        <div>
            <div>{heading}</div>
            <ol>
                {[...wordList].slice(0, 100).map((item, index) => (
                    <li className="optionItem" key={item.word} value={index + 1}
                        onClick={() => onSelectWord(item.word.toUpperCase())}>{item.word + " (" + (item.freqScore * 100).toFixed(0) + "%)"}</li>
                ))}
            </ol>
        </div>
    );
}

export default OptionTab;