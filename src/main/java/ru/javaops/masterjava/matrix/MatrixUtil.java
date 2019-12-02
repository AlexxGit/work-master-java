package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = getLength(matrixA);
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);
        int[] thatColumn = new int[matrixSize];

        return matrixC;
    }

    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = getLength(matrixA);
        final int[][] matrixC = new int[matrixSize][matrixSize];
        int[] thatColumn = new int[matrixSize];

        for (int j = 0; j < matrixSize; j++) {
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][j];
            }
            for (int i = 0; i < matrixSize; i++) {
                int[] thisRow = matrixA[i];
                int sum = 0;
                sum = getMatrixElement(matrixSize, thatColumn, thisRow, sum);
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    private static int getMatrixElement(int matrixSize, int[] thatColumn, int[] thisRow, int sum) {
        for (int k = 0; k < matrixSize; k++) {
            sum += thisRow[k] * thatColumn[k];
        }
        return sum;
    }

    private static int getLength(int[][] matrixA) {
        return matrixA.length;
    }

    private static int[][] transpositionMatrix(int[][] matrix) {
        int matrixSize = getLength(matrix);
        int[][] result = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                result[j][i] = matrix[i][j];
            }
        }
//        System.out.println("After transposition action:");
//        MainMatrix.printMatrix(result);
        return result;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = getLength(matrixA);
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
