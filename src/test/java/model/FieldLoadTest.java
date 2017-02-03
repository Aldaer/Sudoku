package model;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class FieldLoadTest {
    @Test
    public void loadFieldFromFile() throws Exception {
        String[] defaultField = SudokuFieldTest.DEFAULT_FIELD;

        final List<String> lines = Files.readAllLines(Paths.get("src/main/resources/data/default_field.txt"));

        SudokuField loaded = FieldLoader.getFieldFromText(lines);
        SudokuField def = FieldLoader.getFieldFromLines(defaultField);
        assertTrue(def.valuesEqual(loaded));
    }
}
