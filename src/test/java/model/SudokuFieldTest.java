package model;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SudokuFieldTest {
    private SudokuField field;
    private SudokuField fieldSolved;

    static final String[] DEFAULT_FIELD = {
            "--179-2-6",
            "73-21-98-",
            "926-543--",
            "-781-5-9-",
            "31-489--7",
            "54---7128",
            "1-7-62-5-",
            "--5971-3-",
            "2635--7--"};

    private static final String[] DEFAULT_FIELD_SOLVED = {
            "851793246",
            "734216985",
            "926854371",
            "678125493",
            "312489567",
            "549637128",
            "197362854",
            "485971632",
            "263548719"};

    @Before
    public void setUp() throws Exception {
        field = FieldLoader.getFieldFromText(DEFAULT_FIELD);
        fieldSolved = FieldLoader.getFieldFromText(DEFAULT_FIELD_SOLVED);
    }

    @Test
    public void creation1() throws Exception {
        assertThat(field.cells[29].getDefValue(), is(8));
        assertThat(field.cells[66].getDefValue(), is(9));
    }

    @Test
    public void creation2() throws Exception {
        final String szd = field.serialize();
        System.out.println(szd);
        field = new SudokuField(szd);
        creation1();
    }

    @Test
    public void isFilled() throws Exception {
        assertFalse(field.isFilled());
        assertTrue(fieldSolved.isFilled());
    }

    @Test
    public void isValid() throws Exception {
        assertTrue(fieldSolved.isValid());
        final String[] solvedBad = Arrays.copyOf(DEFAULT_FIELD_SOLVED, 9);
        solvedBad[8] = "263584718";
        SudokuField fieldSolvedBad = FieldLoader.getFieldFromText(solvedBad);
        assertFalse(fieldSolvedBad.isValid());
    }



}
