package com.philvarner.clamavj;

public class ScanResult {

    private String result = "";
    private Status status = Status.FAILED;
    private String signature = "";
    private Exception exception = null;

    public enum Status {PASSED, FAILED, ERROR}

    public static final String STREAM_PATH = "stream";
    public static final String OK_SUFFIX = "OK";
    public static final String FOUND_SUFFIX = "FOUND";
    public static final String ERROR_SUFFIX = "ERROR";

    public static final String RESPONSE_SIZE_EXCEEDED = "INSTREAM size limit exceeded. ERROR";
    public static final String RESPONSE_ERROR_WRITING_FILE = "Error writing to temporary file. ERROR";

    public static String getPrefix(String path) {
        return path + ": ";
    }

    public ScanResult(String result, String path) {
        setResult(result, path);
    }

    public ScanResult(Exception ex) {
        setException(ex);
        setStatus(Status.ERROR);
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result, String path) {
        
        this.result = result;
        
        String prefix = getPrefix(path);

        if (result == null) {
            setStatus(Status.ERROR);
        } else if (result.equals(prefix + OK_SUFFIX)) {
            setStatus(Status.PASSED);
        } else if (result.endsWith(FOUND_SUFFIX)) {
            setSignature(result.substring(prefix.length(), result.lastIndexOf(FOUND_SUFFIX) - 1));
        } else if (result.endsWith(ERROR_SUFFIX)) {
            setStatus(Status.ERROR);
        }

    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
