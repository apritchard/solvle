import React from 'react';

function AnagramOptionTab({wordMap, heading}) {

    return (
        <div>
            <div>{heading}</div>
                 {Object.keys(wordMap).reverse().map((key) => (
                     wordMap[key].length < 1 ? <div> </div> :
                     <div key={"anagramsLength" + key}>
                         Length: {key}
                         <ol>
                             {wordMap[key].map((word, index) => (
                                 <li className="optionItem" key={word} value={index + 1}>
                                     {word}</li>
                             ))}
                         </ol>
                     </div>
                    ))}
        </div>);
}

export default AnagramOptionTab;