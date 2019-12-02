package ru.javaops.masterjava.matrix;

public class MatrixResult {
    private static final String OK = "OK";
    private String result;
    private String error;

    private static MatrixResult ok(String result) {
        return new MatrixResult(OK);
    }

    private static MatrixResult error(String result, String error) {
        return new MatrixResult(result, error);
    }

    public boolean isOk() {
        return OK.equals(result);
    }

    public MatrixResult(String result, String error) {
        this.result = result;
        this.error = error;
    }

    public MatrixResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "MatrixResult{" +
                "result='" + result + '\'' +
                '}';
    }
}
