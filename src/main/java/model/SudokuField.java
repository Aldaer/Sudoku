package model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import static model.SudokuCell.*;
import static model.SudokuConstants.SUDOKU_BLOCK_INDEX;

public class SudokuField implements SudokuContainer {
    final SudokuCell[] cells = new SudokuCell[81];

    @Getter
    private final List<SudokuElement> contents = new ArrayList<>(3);

    @Override
    public String getType() {
        return "tbody";
    }

    boolean isFilled() {
        return Arrays.stream(cells)
                .allMatch(SudokuCell::isDefinite);
    }

    boolean isValid() {
        return Arrays.stream(cells)
                .filter(SudokuCell::isDefinite)
                .allMatch(cell -> Arrays.stream(SudokuConstants.AFFECTING_CELL_TABLE[cell.index])
                        .flatMapToInt(Arrays::stream)
                        .mapToObj(i -> cells[i])
                        .mapToInt(SudokuCell::getDefValue)
                        .noneMatch(value -> cell.getDefValue() == value)
                );
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
            int ix = SudokuConstants.HTML_WALK_INDEX[i];
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

    public void generateHints(HintMode hintMode) {
        IntUnaryOperator hintByIndex;
        switch (hintMode) {
            case ON:
                hintByIndex = this::calculateHintedValue;
                break;
            case SMART:
                generateSmartHints();
                return;
            case OFF:
                hintByIndex = i -> cells[i].value & ~HINT_ON;
                break;
            case MANUAL:
            default:
                return;
        }
        for (int i = 0; i < 81; i++)
            cells[i].value = hintByIndex.applyAsInt(i);
    }

    private int calculateHintedValue(int index) {
        int val = cells[index].value | HINT_MASK;
        int used = 0;
        for (int[] block : SudokuConstants.AFFECTING_CELL_TABLE[index])
            for (int j : block)
                used |= hintBit(cells[j].getDefValue());
        return val & ~used;
    }


    private void generateSmartHints() {
        generateHints(HintMode.ON);
        for (SudokuCell cell : cells) {
            if (cell.contradictsHint()) return;
            if (! cell.isDefinite()) continue;
            // For definite cells, set only one bit for possible contents.
            cell.value &= ~HINT_MASK;
            cell.value |= hintBit(cell.getDefValue());
        }

        SudokuCell[] currentBlock = new SudokuCell[9];
        List<SudokuCell> indefiniteInCurrentBlock = new ArrayList<>(9);
        List<SudokuCell> selectedSubGroup = new ArrayList<>(8);
        List<SudokuCell> unselectedSubGroup = new ArrayList<>(8);

        int[] currentBlockValues = new int[9];
        int[] buf = new int[9];
        for (boolean updated = true; updated; ) {
            updated = false;
            for (int[] blockIndex : SUDOKU_BLOCK_INDEX) {
                for (int j = 0; j < 9; j++) {
                    currentBlock[j] = cells[blockIndex[j]];
                    currentBlockValues[j] = currentBlock[j].value;
                }
                if (!isBlockValid(currentBlockValues)) return;

                for (int j = 0; j < 9; j++) {
                    if (currentBlock[j].isDefinite()) continue;
                    int currentCellValue = currentBlock[j].value;
                    for (int n = 0; n < 9; n++) {
                        int nthBit = 1 << n;
                        if ((currentCellValue & nthBit) == 0) continue;
                        System.arraycopy(currentBlockValues, 0, buf, 0, 9);
                        int clearNthBit = ~nthBit;
                        for (int k = 0; k < 9; k++)
                            buf[k] &= clearNthBit;
                        buf[j] |= nthBit;
                        if (!isBlockValid(buf)) {       // Variant that makes block invalid cannot be used, delete from hint
                            currentBlock[j].value &= ~nthBit;
                            updated = true;
                        }

                    }

                }

            }

        }

    }

    private static boolean isBlockValid(int[] block) {
        assert block.length == 9;
        int possibilities = 0;
        boolean empties = false;
        for (int i = 0; i < 9; i++) {
            possibilities |= block[i];
            empties |= (block[i] & HINT_MASK) == 0;
        }
        return !empties && ((possibilities & HINT_MASK) == HINT_MASK);
    }

    boolean valuesEqual(SudokuField f) {
        return IntStream.range(0, 81).allMatch(i -> cells[i].getDefValue() == f.cells[i].getDefValue());
    }

    public void reset() {
        Arrays.stream(cells).forEach(SudokuCell::reset);
    }

    public void activateHints() {
        Arrays.stream(cells).forEach(SudokuCell::activateHint);
    }

}
