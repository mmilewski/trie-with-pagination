package org.example;

import org.example.dictionary.Dictionary;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/*
single user, single machine

If we change X, then others don't change
unit tests

*/

public class MainCLI {
    public static void main(String[] args) throws IOException {


        // read the dictionary into the memory
        if (args.length < 1) {
            throw new IllegalArgumentException("Specify path to the dictionary as the first argument");
        }
        System.out.println("Reading list of words from " + args[0]);  // "/Users/marcin/Desktop/words_alpha.txt"

        final Path dictionarySource = Path.of(args[0]);
        final Dictionary dict = readDictionary(dictionarySource);

        // get requests from the commandline. Later they can come from somewhere else
        processRequestsFromStdin(dict);
    }

    private static void processRequestsFromStdin(Dictionary dict) {
        final Scanner userInputs = new Scanner(System.in);
        while (userInputs.hasNext()) {
            String userInput = userInputs.next();

            try {

                int currentPageNumber = 1;
                int pageSize = 3;
                String lastSeenWord = "";
                while (true) {
                    List<String> matchingWords = dict.lookup(new Dictionary.LookupRequest(userInput, lastSeenWord, pageSize));
                    if (matchingWords.isEmpty()) {
                        break;
                    }
                    System.out.println("Dictionary returned matching words (page " + currentPageNumber + ")");
                    matchingWords.forEach(word -> System.out.println("\t- " + word));

                    lastSeenWord = matchingWords.get(matchingWords.size() - 1);
                    currentPageNumber++;
                }


            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static Dictionary readDictionary(Path filepath) throws IOException {
        return new Dictionary(filepath);
    }
}