package ru.javaops.masterjava.matrix;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * gkislin
 * 03.07.2016
 */
public class MainMatrix {
    private static final int MATRIX_SIZE = 1000;
    private static final int THREAD_NUMBER = 10;

    private final static ExecutorService executor = Executors.newFixedThreadPool(MainMatrix.THREAD_NUMBER);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final int[][] matrixA = MatrixUtil.create(MATRIX_SIZE);
//        System.out.println("matrixA: ");
//        printMatrix(matrixA);
        final int[][] matrixB = MatrixUtil.create(MATRIX_SIZE);
//        System.out.println("matrixB: ");
//        printMatrix(matrixB);

        double singleThreadSum = 0.;
        double concurrentThreadSum = 0.;
        int count = 1;
        while (count < 6) {
            System.out.println("Pass " + count);
            long start = System.currentTimeMillis();
            final int[][] matrixC = MatrixUtil.singleThreadMultiply(matrixA, matrixB);
//            System.out.println("matrixC:");
//            printMatrix(matrixC);
            double duration = (System.currentTimeMillis() - start) / 1000.;
            out("Single thread time, sec: %.3f", duration);
            singleThreadSum += duration;

            start = System.currentTimeMillis();
            final int[][] concurrentMatrixC = MatrixUtil.concurrentMultiply(matrixA, matrixB, executor);
//            System.out.println("concurrentMatrixC:");
//            printMatrix(concurrentMatrixC);
            duration = (System.currentTimeMillis() - start) / 1000.;
            out("Concurrent thread time, sec: %.3f", duration);
            concurrentThreadSum += duration;

            if (!MatrixUtil.compare(matrixC, concurrentMatrixC)) {
                System.err.println("Comparison failed");
                break;
            }
            count++;
        }
        executor.shutdown();
        out("\nAverage single thread time, sec: %.3f", singleThreadSum / 5.);
        out("Average concurrent thread time, sec: %.3f", concurrentThreadSum / 5.);
    }

    private static void out(String format, double ms) {
        System.out.println(String.format(format, ms));
    }

    public static void printMatrix(int[][] matrix) {
        int matrixSize = matrix.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
    }
}
