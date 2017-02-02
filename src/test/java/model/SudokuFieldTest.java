package model;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SudokuFieldTest {
    private SudokuField field;

    private static final String[] SIMPLE_S = {
            "--179-2-6",
            "73-21-98-",
            "926-543--",
            "-781-5-9-",
            "31-489--7",
            "54---7128",
            "1-7-62-5-",
            "--5971-3-",
            "2635--7--"};

    @Before
    public void setUp() throws Exception {
        field = FieldLoader.getFieldFromText(SIMPLE_S);
    }

    @Test
    public void testCreation1() throws Exception {
        assertThat(field.cells[29].getDefValue(), is(8));
        assertThat(field.cells[66].getDefValue(), is(9));
    }

    @Test
    public void testCreation2() throws Exception {
        final String szd = field.serialize();
        System.out.println(szd);
        field = new SudokuField(szd);
        testCreation1();
    }


}
