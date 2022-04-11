package com.appsoil.solvle.config;

import com.appsoil.solvle.wordler.Dictionary;
import com.appsoil.solvle.wordler.Word;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Configuration
@Log4j2
public class SolvleConfig {

    @Bean
    Dictionary getDictionary() {
        log.info("Current path: " + System.getProperty("user.dir"));
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
                    log.info(count - 1 + " read...");
                }
            }
        } catch (IOException ioe) {
            log.info("Error parsing dictionary");
            throw new RuntimeException(ioe);
        }

        log.info("Read in " + words.size() + " words");
        return new Dictionary(words);
    }
}
