package com.appsoil.solvle.config;

import com.appsoil.solvle.wordler.Dictionary;
import com.appsoil.solvle.wordler.Word;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class SolvleConfig {

    // @todo add log4j

    @Bean
    Dictionary getDictionary() {
        System.out.println("Current path: " + System.getProperty("user.dir"));
        InputStream is = this.getClass().getResourceAsStream("/dict2/enable1.txt");
        Set<Word> words = new HashSet<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        int count = 0;

        try {
            String word = br.readLine();
            while (word != null) {
                words.add(new Word(word));
                word = br.readLine();
                if (count++ % 10000 == 0) {
                    System.out.println(count - 1 + " read...");
                }
            }
        } catch (IOException ioe) {
            System.out.printf("Error parsing dictionary");
            throw new RuntimeException(ioe);
        }

        System.out.println("Read in " + words.size() + " words");
        return new Dictionary(words);
    }
}
