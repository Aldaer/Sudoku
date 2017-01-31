package model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.IntUnaryOperator;

import static model.SudokuCell.*;

@RequiredArgsConstructor
public class SudokuField implements SudokuContainer {
    final SudokuCell[] cells = new SudokuCell[81];

    @Getter
    private final List<SudokuElement> contents = new ArrayList<>(3);

    @Override
    public String getType() {
        return "tbody";
    }

    public boolean solved() {
        boolean slv = true;
        for (SudokuCell cell : cells) {
            slv &= cell.isDefinite();
        }
        return slv;
    }

    List<List<SudokuCell>> getSudokuBlocks() {
        List<List<SudokuCell>> result = new ArrayList<>(27);
        for (int i = 0; i < 9; i++) {
            List<SudokuCell> row = new ArrayList<>(9);
            List<SudokuCell> col = new ArrayList<>(9);
            List<SudokuCell> box = new ArrayList<>(9);
            int boxStart = (i % 3) * 3 + (i / 3) * 27;                  // 0 3 6 27 30 33 54 57 60
            for (int j = 0; j < 9; j++) {
                row.add(cells[i * 9 + j]);
                col.add(cells[i + j * 9]);
                box.add(cells[boxStart + j % 3 + (j / 3) * 9]);        // 0 1 2 9 10 11 18 19 20
            }
            result.add(row);
            result.add(col);
            result.add(box);
        }
        return result;
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


    public SudokuField(String serialized) {
        this(decode(serialized));
    }

    private static int[] decode(String serialized) {
        final byte[] bytes = Base64.getDecoder().decode(serialized);
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

    SudokuField(String[] textRepresentation) {
        this(textToArray(textRepresentation));
    }

    private static int[] textToArray(String[] textRepresentation) {
        assert textRepresentation.length == 9;

        int[] values = new int[81];
        for (int row = 0; row < 9; row++) {
            assert textRepresentation[row].length() == 9;

            for (int col = 0; col < 9; col++) {
                int strNum = (int) (textRepresentation[row].charAt(col)) - '0';
                int num = (strNum < 0 || strNum > 9) ? 0 : hardcodedValue(strNum);
                values[row * 9 + col] = num;
            }
        }
        return values;
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
    private SudokuField(int[] values) {
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
        final byte[] bytes = new byte[324];
        for (int i = 0; i < 81; i++) {
            int x = cells[i].value;
            for (int k = 3; k >= 0; k--) {
                bytes[i * 4 + k] = (byte) (x & 255);
                x >>= 8;
            }
        }
        return new String(Base64.getEncoder().encode(bytes));
    }

    public static SudokuField getDefaultField() {
        String[] sx = {
                "--179-2-6",
                "73-21-98-",
                "926-543--",
                "-781-5-9-",
                "31-489--7",
                "54---7128",
                "1-7-62-5-",
                "--5971-3-",
                "2635--7--"};
        return new SudokuField(sx);
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
}
