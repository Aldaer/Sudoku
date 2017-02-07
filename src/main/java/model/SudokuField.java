package model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import static model.SudokuCell.*;
import static model.SudokuConstants.SUDOKU_BLOCK_INDEX;
import static model.util.Combinatorics.*;

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

    public void setCellValue(String cell, String value, int colorCode) {
        try {
            int cellNum = Integer.parseInt(cell);
            if (cellNum < 0 || cellNum > 80) return;
            int val = value.equals("") ? 0 : Integer.parseInt(value);

            if (val < 0 || val > 9) return;
            cells[cellNum].setDefiniteValue(val);
            cells[cellNum].setColorCode(colorCode);
        } catch (NumberFormatException ignored) {
        }
    }

    public void generateHints(HintMode hintMode) {
        IntUnaryOperator hintByIndex;

        unmarkAllCellsAsBad();

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

        final List<SudokuCell> indefiniteInCurrentBlock = new ArrayList<>(9);
        final List<Integer> previousValues = new ArrayList<>(9);

        for (boolean updated = true; updated; ) {
            updated = false;
            blockLoop:
            for (int[] blockIndex : SUDOKU_BLOCK_INDEX) {
                indefiniteInCurrentBlock.clear();
                previousValues.clear();
                for (int index : blockIndex)
                    if (!cells[index].isDefinite()) {
                        indefiniteInCurrentBlock.add(cells[index]);
                        previousValues.add(cells[index].value);
                    }

                int numIndefs = indefiniteInCurrentBlock.size();
                for (int groupSize = 1; groupSize < numIndefs; groupSize++) {
                    final int[][] combinations = COMBINATIONS[numIndefs][groupSize];
                    final int[][] complements = ANTI_COMBINATIONS[numIndefs][groupSize];
                    for (int combinationIndex = 0; combinationIndex < combinations.length; combinationIndex++) {
                        final int combinedBitsInGroup = IntStream.of(combinations[combinationIndex])
                                .mapToObj(indefiniteInCurrentBlock::get)
                                .mapToInt(SudokuCell::hintValue)
                                .reduce(0, (x, y) -> x | y);
                        int nSet = BIT_COUNT[combinedBitsInGroup];
                        if (nSet < groupSize) {
                            markCellsAsBad(blockIndex);
                            continue blockLoop;
                        }
                        if (nSet == groupSize) {            // Set bits are exhausted by this combination of cells
                            int antiBits = ~combinedBitsInGroup;
                            IntStream.of(complements[combinationIndex])
                                    .mapToObj(indefiniteInCurrentBlock::get)
                                    .forEach(sudokuCell -> sudokuCell.value &= antiBits);
                        }
                    }

                }
                for (int indef = 0; indef < numIndefs; indef++)
                    updated |= indefiniteInCurrentBlock.get(indef).value != previousValues.get(indef);
            }
        }
    }

    private void markCellsAsBad(int[] blockIndex) {
        for (int i : blockIndex) {
            cells[i].value |= HINT_INCONSISTENCE;
        }
    }

    private void unmarkAllCellsAsBad() {
        for (SudokuCell cell : cells)
            cell.value &= ~HINT_INCONSISTENCE;
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
