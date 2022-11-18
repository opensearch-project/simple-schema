package org.opensearch.schema;


import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * general purpose error
 */
public class SchemaError {
    private String errorCode;
    private String errorDescription;

    public SchemaError() {
    }

    public SchemaError(String errorCode, Throwable e) {
        this.errorCode = errorCode;
        StringWriter sw = new StringWriter();
        if (e != null) {
            this.errorDescription = e.getMessage() != null ? e.getMessage() : sw.toString();
        }
    }

    public SchemaError(String errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    @Override
    public String toString() {
        return "SchemaError{" +
                "errorCode='" + errorCode + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }


    /**
     * the exception raised inside the schema error
     */
    public static class SchemaErrorException extends RuntimeException {
        private final SchemaError error;

        public SchemaErrorException(String message, SchemaError error) {
            super(message);
            this.error = error;
        }

        public SchemaErrorException(String error, String description) {
            super();
            this.error = new SchemaError(error, description);
        }

        public SchemaErrorException(SchemaError error) {
            super();
            this.error = error;
        }

        public SchemaErrorException(String message, Throwable cause, SchemaError error) {
            super(message, cause);
            this.error = error;
        }

        public SchemaErrorException(String message, Throwable cause) {
            super(message, cause);
            this.error = new SchemaError(message, cause);
        }

        public SchemaError getError() {
            return error;
        }

        @Override
        public String toString() {
            return "SchemaErrorException{" +
                    "error=" + error +
                    '}';
        }
    }
}
