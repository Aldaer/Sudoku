package model;

import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.IntUnaryOperator;

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
            final List<String> strs = IOUtils.readLines(Objects.requireNonNull(FieldLoader.class
                    .getClassLoader().getResourceAsStream("data/default_field.txt")), StandardCharsets.UTF_8);

            return getFieldFromText(strs);
        } catch (InvalidFieldDataException e) {
            throw new RuntimeException(e);
        }
    }

    static SudokuField getFieldFromLines(String[] normalizedRepresentation) throws InvalidFieldDataException {
        return new SudokuField(textToArray(normalizedRepresentation));
    }

    static SudokuField getFieldFromText(List<String> lines) throws InvalidFieldDataException {
        return getFieldFromLines(normalizeLines(lines));
    }

    private static String[] normalizeLines(List<String> lines) {
        return lines.stream()
                .flatMap(s -> Arrays.stream(s.split("[\n\r]")))
                .map(s -> s.replaceAll("\\s+", ""))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    public static SudokuField deserializeField(String base64encoded) {
        return new SudokuField(decode(base64encoded));
    }

    public static SudokuField getEmptyField() {
        return new SudokuField(new int[81]);
    }

    public static SudokuField getFieldFromString(String contents) throws InvalidFieldDataException {
        return getFieldFromText(Collections.singletonList(contents));
    }

    static String encode(IntUnaryOperator valueFromIndex) {
        final byte[] bytes = new byte[324];
        for (int i = 0; i < 81; i++) {
            int x = valueFromIndex.applyAsInt(i);
            for (int k = 3; k >= 0; k--) {
                bytes[i * 4 + k] = (byte) (x & 255);
                x >>= 8;
            }
        }
        return new String(Base64.getEncoder().encode(bytes));
    }

    private static int[] decode(String encoded) {
        final byte[] bytes = Base64.getDecoder().decode(encoded);
        assert bytes.length == 324;

        int[] values = new int[81];
        for (int i = 0; i < 81; i++) {
            int x = 0;
            for (int k = 0; k < 4; k++) {
                x <<= 8;
                x += (bytes[i * 4 + k] + 256) & 255;
            }
            values[i] = x;
        }
        return values;
    }
}

