# Solvle - A Word Puzzle Analysis Tool

Try it out at http://solvle.appsoil.com

This is a toy project that I made to learn React. It's the first React app I've written, 
and its initial shell borrows heavily from the Wordle-Clone-React tutorial, available on github:
https://github.com/machadop1407/Wordle-Clone-React

The backend for this application uses Spring Boot. It would probably have made sense to in-line
the analysis logic into the frontend and just download a dictionary to each user, but I
wanted to learn how to make a React app integrate with a Spring Boot controller, so this design choice
was strictly for educational value.

## Running the Application

There are two components to this application:
1. The Spring Boot server. Assuming you have Java installed, you should simply be able to run
the SolvleApplication class to launch the backend application. You can change the port in
application.properties, but make sure to update setupProxy.js if you do.
2. A react application. Assuming you have npm installed, run 'npm start' from the
/solvle-front directory. It launches on port 80 by default, but you can edit the "start" script in 
package.json to change this.

## Using the Application
The design is intended to be familiar to users of other similar word guessing games.
1. Type letters or tap the on-screen keyboard to enter a word.
2. Click the letters you've entered to mark them gray, yellow, or green (unavailable, wrong position, or correct).
3. Press ENTER to advance the word choice to the next line.

The application will calculate optimal word choices based on the number of viable remaining words that
contain the letters of each word suggestion. It also displays the number of viable words containing
each letter of the alphabet as small numbers below the keyboard letters.

Lastly, the app calculates 'Fishing' words, which are words that may not be viable words, but contain
the highest frequency of *new* letters found in the remaining word options. This can be useful if
you don't think you can correctly guess on the next word and want to maximize the amount of 
information gained.

## Dictionaries Used
There are currently 4 dictionaries supported by this application.
1. The Wordle Solutions list. It contains 2315 words. I pulled this particular set of words from
https://github.com/techtribeyt/Wordle/blob/main/wordle_answers.txt
2. (deprecated) The Match-8 wordlist, which contains 108814 words found in at least 8 of the word corpus files
aggregated here: https://www.keithv.com/software/wlist/
3. The Official Scrabble Player's Dictionary, containing 172819 words found here: 172820 words: https://github.com/dolph/dictionary/
4. The InfoChimps 370103 words dataset, found in text here: https://github.com/dwyl/english-words/

The simple wordle solutions list is the default dictionary, but the scrabble dictionary is used as the
medium size choice because it contains more normal word list than the match-8.
