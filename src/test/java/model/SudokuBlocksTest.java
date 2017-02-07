package model;

import org.junit.Test;

import static model.SudokuConstants.SUDOKU_BLOCK_INDEX;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SudokuBlocksTest {
    @Test
    public void rows() throws Exception {
        int[] row0 = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        assertThat(SUDOKU_BLOCK_INDEX[0], is(row0));
    }

    @Test
    public void columns() throws Exception {
        int[] col1 = {1, 10, 19, 28, 37, 46, 55, 64, 73};
        assertThat(SUDOKU_BLOCK_INDEX[10], is(col1));
    }

    @Test
    public void boxes() throws Exception {
        int[] box2 = {6, 7, 8, 15, 16, 17, 24, 25, 26};
        assertThat(SUDOKU_BLOCK_INDEX[20], is(box2));
    }
}
