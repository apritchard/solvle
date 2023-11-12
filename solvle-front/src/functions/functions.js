
export function generateRestrictionString(availableLetters, knownLetters, unsureLetters) {
    let restrictionString = "";

    "AÁBCDÐEÉFGHIÍJKLMNOÓPQRSTUÚVWXYÝZÞÆÖ".split("").filter(letter => availableLetters.has(letter)).forEach(letter => {
        restrictionString += letter;
        knownLetters.forEach((l, pos) => {
            if (l === letter) {
                console.log("Known letter " + letter + " pos " + (pos + 1));
                restrictionString += (pos + 1);
            }
        });
        let hasUnsure = false;
        unsureLetters.forEach((letters, pos) => {
            if (letters.has(letter)) {
                if (!hasUnsure) {
                    hasUnsure = true;
                    restrictionString += "!";
                }
                console.log("unsure letter " + letter + " pos " + (pos + 1));
                restrictionString += (pos + 1);
            }
        });
    })
    return restrictionString;
}

export function generateAnagramString(board) {
    let anagramString = "";

    for(let i = 0; i < board.length ; i++) {
        for(let j = 0; j < board[i].length; j++) {
            if(board[i][j] !== '') {
                anagramString += board[i][j];
            }
        }
    }

    return anagramString;
}

export function generateConfigParams(boardState) {
    let hardMode = boardState.settings.hardMode ?
        "&hardMode=true" : "&hardMode=false";

    let biasParams = boardState.settings.useBias ?
        "&rightLocationMultiplier=" + boardState.settings.calculationConfig.rightLocationMultiplier +
        "&uniquenessMultiplier=" + boardState.settings.calculationConfig.uniquenessMultiplier +
        "&viableWordPreference=" + boardState.settings.calculationConfig.viableWordPreference
        : "&rightLocationMultiplier=0&uniquenessMultiplier=0&viableWordPreference=0";

    let partitionParams = boardState.settings.usePartitioning ?
        "&partitionThreshold=" + boardState.settings.calculationConfig.partitionThreshold
        : "&partitionThreshold=0";

    let fineTuningParams = boardState.settings.useBias && boardState.settings.useFineTuning ?
        "&locationAdjustmentScale=" + boardState.settings.calculationConfig.locationAdjustmentScale +
        "&uniqueAdjustmentScale=" + boardState.settings.calculationConfig.uniqueAdjustmentScale +
        "&viableWordAdjustmentScale=" + boardState.settings.calculationConfig.viableWordAdjustmentScale +
        "&vowelMultiplier=" + boardState.settings.calculationConfig.vowelMultiplier
        : "&locationAdjustmentScale=0&uniqueAdjustmentScale=0&viableWordAdjustmentScale=0&vowelMultiplier=1"

    let rutIdParams = boardState.settings.useRutBreaking ?
        "&rutBreakMultiplier=" + boardState.settings.calculationConfig.rutBreakMultiplier +
        "&rutBreakThreshold=" + boardState.settings.calculationConfig.rutBreakThreshold
        : "&rutBreakMultiplier=0&rutBreakThreshold=0";

    return hardMode + biasParams + partitionParams + fineTuningParams + rutIdParams;
}