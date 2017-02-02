package model;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static model.SudokuCell.hardcodedValue;

public class FieldLoader {
    private static int[] textToArray(String[] textRepresentation) throws InvalidFieldDataException {
        try {
            int[] values = new int[81];
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int strNum = (int) (textRepresentation[row].charAt(col)) - '0';
                    int num = (strNum < 0 || strNum > 9) ? 0 : hardcodedValue(strNum);
                    values[row * 9 + col] = num;
                }
            }
            return values;
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidFieldDataException(e);
        }
    }

    public static SudokuField getDefaultField() {
        try {
            final List<String> strs = IOUtils.readLines(FieldLoader.class
                    .getClassLoader().getResourceAsStream("data/default_field.txt"), StandardCharsets.UTF_8);

            return getFieldFromText(strs);
        } catch (IOException | InvalidFieldDataException e) {
            throw new RuntimeException(e);
        }
    }

    static SudokuField getFieldFromText(String[] normalizedRepresentation) throws InvalidFieldDataException {
        return new SudokuField(textToArray(normalizedRepresentation));
    }

    static SudokuField getFieldFromText(List<String> lines) throws InvalidFieldDataException {
        return getFieldFromText(normalizeLines(lines));
    }

    private static String[] normalizeLines(List<String> lines) {
        return lines.stream()
                .map(s -> s.replaceAll("\\s", ""))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}

class InvalidFieldDataException extends Exception {
    InvalidFieldDataException(Throwable cause) {
        super(cause);
    }
}
