package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatrixUtil {

    public static int[][] concurrentMultiplyWithIntStream(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = getLength(matrixA);
        final int[][] matrixC = new int[matrixSize][matrixSize];

        List<Callable<Void>> tasks = IntStream.range(0, matrixSize)
                .mapToObj(j -> new Callable<Void>() {
                    int[] thatColumn = new int[matrixSize];

                    @Override
                    public Void call() {
                        for (int k = 0; k < matrixSize; k++) {
                            thatColumn[k] = matrixB[k][j];
                        }
                        for (int i = 0; i < matrixSize; i++) {
                            int[] thisRow = matrixA[i];
                            int sum = 0;
                            for (int k = 0; k < matrixSize; k++) {
                                sum += thisRow[k] * thatColumn[k];
                            }
                            matrixC[i][j] = sum;
                        }
                        return null;
                    }
                })
                .collect(Collectors.toList());
        executor.invokeAll(tasks);
        return matrixC;
    }

    public static int[][] concurrentMultiply2(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][];

        final int[][] matrixBT = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixBT[i][j] = matrixB[j][i];
            }
        }

        List<Callable<Void>> tasks = new ArrayList<>(matrixSize);
        for (int j = 0; j < matrixSize; j++) {
            final int row = j;
            tasks.add(() -> {
                final int[] rowC = new int[matrixSize];
                for (int col = 0; col < matrixSize; col++) {
                    final int[] rowA = matrixA[row];
                    final int[] columnB = matrixBT[col];
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += rowA[k] * columnB[k];
                    }
                    rowC[col] = sum;
                }
                matrixC[row] = rowC;
                return null;
            });
        }
        executor.invokeAll(tasks);
        return matrixC;
    }

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final CountDownLatch latch = new CountDownLatch(matrixSize);

        for (int row = 0; row < matrixSize; row++) {
            final int[] rowA = matrixA[row];
            final int[] rowC = matrixC[row];

            executor.submit(() -> {
                for (int idx = 0; idx < matrixSize; idx++) {
                    final int elA = rowA[idx];
                    final int[] rowB = matrixB[idx];
                    for (int col = 0; col < matrixSize; col++) {
                        rowC[col] += elA * rowB[col];
                    }
                }
                latch.countDown();
            });
        }
        latch.await();
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
                for (int k = 0; k < matrixSize; k++) {
                    sum += thisRow[k] * thatColumn[k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    private static int getLength(int[][] matrixA) {
        return matrixA.length;
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

    private static class ColumnMultipleResult {
        private final int column;
        private final int[] columnC;

        public ColumnMultipleResult(int column, int[] columnC) {
            this.column = column;
            this.columnC = columnC;
        }
    }
}
