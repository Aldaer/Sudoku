package model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import static model.SudokuCell.HINT_MASK;
import static model.SudokuCell.HINT_ON;

public class SudokuField implements SudokuContainer {
    final SudokuCell[] cells = new SudokuCell[81];

    @Getter
    private final List<SudokuElement> contents = new ArrayList<>(3);

    @Override
    public String getType() {
        return "tbody";
    }

    public boolean isFilled() {
        return Arrays.stream(cells)
                .allMatch(SudokuCell::isDefinite);
    }

    public boolean isValid() {
        return Arrays.stream(cells)
                .filter(SudokuCell::isDefinite)
                .allMatch(cell -> Arrays.stream(cellsAffectingCellAt(cell.index))
                        .mapToObj(i -> cells[i])
                        .mapToInt(SudokuCell::getDefValue)
                        .noneMatch(value -> cell.getDefValue() == value)
                );
    }

    private final int[][] affectingCellCache = new int[81][];

    private int[] cellsAffectingCellAt(int index) {
        int[] affectingList;
        synchronized (affectingCellCache) {
            affectingList = affectingCellCache[index];
            if (affectingList != null) return affectingList;
            affectingList = new int[24];
            int r = 0;
            int c = 8;
            int b = 16;
            int rowStart = (index / 9) * 9;
            int colStart = index % 9;
            int boxStart = (colStart / 3) * 3 + (rowStart / 27) * 27;
            for (int i = 0; i < 9; i++) {
                int rowI = rowStart + i;
                int colI = colStart + i * 9;
                int boxI = boxStart + i % 3 + (i / 3) * 9;
                if (rowI != index) affectingList[r++] = rowI;
                if (colI != index) affectingList[c++] = colI;
                if (boxI != index) affectingList[b++] = boxI;
            }
            affectingCellCache[index] = affectingList;
        }
        return affectingList;
    }

    private static final int[] HTML_WALK_INDEX = generateHtmlTableWalkIndex();

    // Converts row-column indexing of the text representation into box-by-box thorough indexing of the html
    private static int[] generateHtmlTableWalkIndex() {
        int[] index = new int[81];
        for (int row = 0; row < 9; row++)
            for (int col = 0; col < 9; col++)
                index[row * 9 + col] = (row / 3) * 27 + (row % 3) * 3 + (col / 3) * 9 + col % 3;
        return index;
    }

    @SuppressWarnings("ConstantConditions")
    SudokuField(int[] values) {
        assert values.length == 81;

        SudokuContainer smallRow = null;
        SudokuContainer box = null;
        SudokuContainer bigCell;
        SudokuContainer bigRow = null;

        for (int i = 0; i < 81; i++) {
            if (i % 27 == 0) {
                bigRow = new SudokuContainerImpl("tr");
                contents.add(bigRow);
            }
            if (i % 9 == 0) {
                bigCell = new SudokuContainerImpl("td");
                box = new SudokuContainerImpl("table");
                bigCell.getContents().add(box);
                bigRow.getContents().add(bigCell);
            }
            if (i % 3 == 0) {
                smallRow = new SudokuContainerImpl("tr");
                box.getContents().add(smallRow);
            }
            int ix = HTML_WALK_INDEX[i];
            cells[ix] = new SudokuCell(ix, values[ix]);
            smallRow.getContents().add(cells[ix]);
        }
    }

    public String serialize() {
        return FieldLoader.encode(i -> cells[i].value);
    }

    public void setCellValue(String cell, String value) {
        try {
            int cellNum = Integer.parseInt(cell);
            if (cellNum < 0 || cellNum > 80) return;
            int val = value.equals("") ? 0 : Integer.parseInt(value);

            if (val < 0 || val > 9) return;
            cells[cellNum].setDefiniteValue(val);
        } catch (NumberFormatException ignored) {
        }
    }

    public void generateHints() {
        for (SudokuCell cell : cells) {
            cell.value |= HINT_MASK;
            int used = 0;
            for (int i : cellsAffectingCellAt(cell.index))
                used |= 1 << cells[i].getDefValue() >> 1;
            cell.value &= ~used;
        }
    }

    public void setHintMode(boolean show) {
        final IntUnaryOperator toggleHint = show ?
                v -> v |= HINT_ON :
                v -> v &= ~HINT_ON;
        for (SudokuCell cell : cells)
            cell.value = toggleHint.applyAsInt(cell.value);
    }

    boolean valuesEqual(SudokuField f) {
        return IntStream.range(0, 81).allMatch(i -> cells[i].getDefValue() == f.cells[i].getDefValue());
    }

    public void reset() {
        for (SudokuCell cell : cells)
            cell.reset();
    }
}
