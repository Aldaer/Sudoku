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
        this(buildFromText(textRepresentation));
    }

    private static int[] buildFromText(String[] textRepresentation) {
        assert textRepresentation.length == 9;

        int[] values = new int[81];
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int strNum = (int) (textRepresentation[row].charAt(col)) - '0';
                int num = (strNum < 0 || strNum > 9) ? 0 : hardcodedValue(strNum);
                values[row * 9 + col] = num;
            }
        }
        return values;
    }

    @SuppressWarnings("ConstantConditions")
    private SudokuField(int[] values) {
        assert values.length == 81;

        SudokuContainer smallRow = null;
        SudokuContainer box = null;
        SudokuContainer bigCell;
        SudokuContainer bigRow = null;

        for (int i = 0; i < 81; i++) {
            cells[i] = new SudokuCell(i, values[i]);

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
            smallRow.getContents().add(cells[i]);
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
            if (val != 0) val = SudokuCell.definiteValue(val);
            cells[cellNum].value = val;
        } catch (NumberFormatException ignored) {
        }
    }

    public void generateHints() {
        for (SudokuCell cell : cells)
            cell.value |= HINT_MASK;

        for (List<SudokuCell> sBlock : getSudokuBlocks()) {
            int unused = 0xFFFFFFFF;
            for (SudokuCell cell : sBlock)
                unused &= ~(1 << cell.getDefValue() >> 1);
            for (SudokuCell cell : sBlock)
                cell.value &= unused;
        }
    }

    private static final IntUnaryOperator ACTIVATE_HINTS = v -> v |= HINT_ON;
    private static final IntUnaryOperator DEACTIVATE_HINTS = v -> v &= ~HINT_ON;

    public void setHintMode(boolean show) {
        final IntUnaryOperator op = show ? ACTIVATE_HINTS : DEACTIVATE_HINTS;
        for (SudokuCell cell : cells)
            cell.value = op.applyAsInt(cell.value);
    }
}
