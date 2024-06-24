package no.fintlabs.flyt.azure;

import java.util.Locale;

public class StringUtils {

    public static String capitalizeFirstLetterOfEachWord(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.split("\\s+");
        StringBuilder capitalizedString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedString.append(word.substring(0, 1).toUpperCase(Locale.ROOT))
                        .append(word.substring(1).toLowerCase(Locale.ROOT))
                        .append(" ");
            }
        }

        return capitalizedString.toString().trim();
    }
}