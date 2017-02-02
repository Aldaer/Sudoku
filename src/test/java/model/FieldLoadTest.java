package model;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FieldLoadTest {
    @Test
    public void testFieldFromFile() throws Exception {
        String[] defaultField = {
                "--179-2-6",
                "73-21-98-",
                "926-543--",
                "-781-5-9-",
                "31-489--7",
                "54---7128",
                "1-7-62-5-",
                "--5971-3-",
                "2635--7--"};

        final List<String> lines = Files.readAllLines(Paths.get("src/main/resources/data/default_field.txt"));

        SudokuField loaded = FieldLoader.getFieldFromText(lines);
        SudokuField def = FieldLoader.getFieldFromText(defaultField);
        for (int i = 0; i < 81; i++) {
            assertEquals(loaded.cells[i].value, def.cells[i].value);
        }
    }
}
