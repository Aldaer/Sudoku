package model;

import java.util.function.IntUnaryOperator;

class SudokuConstants {
    private static final BlockCalculator[] INDEX_CALC = {
            BlockCalculator.createInstance( // Row
                    ix -> (ix / 9) * 9,
                    ox -> ox,
                    n -> n * 9),
            BlockCalculator.createInstance( // Column
                    ix -> ix % 9,
                    ox -> ox * 9,
                    n -> n),
            BlockCalculator.createInstance( // Box
                    ix -> ix % 9 / 3 * 3 + ix / 9 * 9 / 27 * 27,
                    ox -> ox % 3 + ox / 3 * 9,
                    n -> n % 3 * 3 + n / 3 * 27)};


    static final int[][][] AFFECTING_CELL_TABLE = generateAffectingCellTable(); // 81 x 3 x 8
    static final int[] HTML_WALK_INDEX = generateHtmlTableWalkIndex();          // 81
    static final int[][] SUDOKU_BLOCK_INDEX = generateSudokuBlockIndex();       // 27 x 9


    private static int[][][] generateAffectingCellTable() {
        int[][][] table = new int[81][][];

        for (int index = 0; index < 81; index++) {
            int[][] affectingBlocks = new int[3][];

            for (int blockType = 0; blockType < 3; blockType++) {
                BlockCalculator calc = INDEX_CALC[blockType];
                int blockStart = calc.getStart(index);

                affectingBlocks[blockType] = new int[8];
                int offs = 0;

                for (int i = 0; i < 9; i++) {
                    int blockIndex = blockStart + calc.getOffset(i);
                    if (blockIndex != index) affectingBlocks[blockType][offs++] = blockIndex;
                }
            }
            table[index] = affectingBlocks;
        }
        return table;
    }

    // Converts row-column indexing of the text representation into box-by-box thorough indexing of the html
    private static int[] generateHtmlTableWalkIndex() {
        int[] index = new int[81];
        for (int row = 0; row < 9; row++)
            for (int col = 0; col < 9; col++)
                index[row * 9 + col] = (row / 3) * 27 + (row % 3) * 3 + (col / 3) * 9 + col % 3;
        return index;
    }

    private static int[][] generateSudokuBlockIndex() {
        int[][] blocks = new int[27][];
        for (int type = 0; type < 3; type++) {
            BlockCalculator calc = INDEX_CALC[type];
            for (int n = 0; n < 9; n++) {
                int blockStart = calc.getBlockStart(n);
                int[] currentBlock = new int[9];
                for (int i = 0; i < 9; i++)
                    currentBlock[i] = blockStart + calc.getOffset(i);

                blocks[type * 9 + n] = currentBlock;
            }
        }
        return blocks;
    }
}

interface BlockCalculator {
    int getStart(int cellIndex);

    int getOffset(int index);

    int getBlockStart(int n);

    static BlockCalculator createInstance(IntUnaryOperator start, IntUnaryOperator offset, IntUnaryOperator blockStart) {
        return new BlockCalculator() {
            @Override
            public int getStart(int cellIndex) {
                return start.applyAsInt(cellIndex);
            }

            @Override
            public int getOffset(int index) {
                return offset.applyAsInt(index);
            }

            @Override
            public int getBlockStart(int n) {
                return blockStart.applyAsInt(n);
            }
        };
    }
}




