package model.util;

import model.SudokuCell;

import java.util.stream.IntStream;

class Combinatorics {
    private static final int MAX_N = 9;
    public static final int[] BIT_COUNT = generatiBitCountMatrix();

    static final int[] FACTORIALS = generateFactorials(MAX_N);


    /**
     * COMBINATIONS[n][k].size() == C(n,k), k <= n
     * COMBINATIONS[n][k][...] - all possible combinations of k integers from the range [1, n]
     * COMBINATIONS[n][k][i][...] - i-th possible combination, 0 <= i < C(n, k)
     * COMBINATIONS[n][k][i][j] - j-th member of the combination, 0 <= j < k
     *
     * COMBINATIONS[0], COMBINATIONS[i][0] == null
     */
    static final int[][][][] COMBINATIONS = generateCombinationsMatrix(9);


    private static int[] generateFactorials(int maxN) {
        int[] result = new int[maxN + 1];
        int base = 1;
        for (int i = 0; i <= maxN; i++) {
            result[i] = base;
            base *= i + 1;
        }
        return result;
    }

    private static int[] generatiBitCountMatrix() {
        return IntStream.rangeClosed(0, SudokuCell.HINT_MASK)
                .map(Integer::bitCount)
                .toArray();
    }


    private static int[][][][] generateCombinationsMatrix(int maxN) {
        int[][][][] matrix = new int[maxN + 1][][][];
        for (int i = 1; i <= maxN; i++)
            fillCombinations(maxN, i, matrix);

        return matrix;
    }

    private static void fillCombinations(int n, int k, int[][][][] matrix) {
        assert n > 0 && n <= MAX_N;
        assert k > 0 && k <= n;

        if ((matrix[n] != null) && matrix[n][k] != null) return;

        if (k == 1) {
            if (matrix[n] == null) matrix[n] = new int[n + 1][][];
            matrix[n][1] = new int[n][];
            for (int i = 0; i < n; i++)
                matrix[n][1][i] = new int[]{i};
            return;
        }
        fillCombinations(n - 1, k - 1, matrix);

        int[][] baseMatrix = matrix[n - 1][k - 1];
        int[][] Cnk = new int[FACTORIALS[n] / (FACTORIALS[n - k] * FACTORIALS[k])][];
        int index = 0;
        for (int[] subArray : baseMatrix) {
            int maxOfSubarray = subArray[k - 2];
            for (int j = maxOfSubarray + 1; j < n; j++) {
                Cnk[index] = new int[k];
                System.arraycopy(subArray, 0, Cnk[index], 0, k - 1);
                Cnk[index][k - 1] = j;
                index++;
            }
        }
        matrix[n][k] = Cnk;
    }

}
