
export function generateRestrictionString(availableLetters, knownLetters, unsureLetters) {
    let restrictionString = "";

    "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("").filter(letter => availableLetters.has(letter)).forEach(letter => {
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

export function generateConfigParams(boardState) {
    let biasParams = boardState.settings.useBias ?
        "&rightLocationMultiplier=" + boardState.settings.calculationConfig.rightLocationMultiplier +
        "&uniquenessMultiplier=" + boardState.settings.calculationConfig.uniquenessMultiplier +
        "&viableWordPreference=" + boardState.settings.calculationConfig.viableWordPreference
        : "&rightLocationMultiplier=0&uniquenessMultiplier=0&viableWordPreference=0";

    let partitionParams = boardState.settings.usePartitioning ?
        "&partitionThreshold=" + boardState.settings.calculationConfig.partitionThreshold
        : "&partitionThreshold=0";

    return biasParams + partitionParams;
}