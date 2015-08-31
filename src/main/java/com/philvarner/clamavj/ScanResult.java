package com.philvarner.clamavj;

public class ScanResult {

    private String result = "";
    private Status status = Status.FAILED;
    private String signature = "";
    private Exception exception = null;

    public enum Status {PASSED, FAILED, ERROR}

    public static final String STREAM_PREFIX = "stream: ";
    public static final String RESPONSE_OK = "stream: OK";
    public static final String FOUND_SUFFIX = "FOUND";
    public static final String ERROR_SUFFIX = "ERROR";

    public static final String RESPONSE_SIZE_EXCEEDED = "INSTREAM size limit exceeded. ERROR";

    public ScanResult(String result) {
        setResult(result);
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

    public void setResult(String result) {
        this.result = result;

        if (result == null) {
            setStatus(Status.ERROR);
        } else if (RESPONSE_OK.equals(result)) {
            setStatus(Status.PASSED);
        } else if (result.endsWith(FOUND_SUFFIX)) {
            setSignature(result.substring(STREAM_PREFIX.length(), result.lastIndexOf(FOUND_SUFFIX) - 1));
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
