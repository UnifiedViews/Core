package eu.unifiedviews.master.model;

public class ErrorResponse {

    private String errorMessage;

    private String technicalMessage;

    public ErrorResponse() {
    }

    public ErrorResponse(String errorMessage, String technicalMessage) {
        this.errorMessage = errorMessage;
        this.technicalMessage = technicalMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTechnicalMessage() {
        return technicalMessage;
    }

    public void setTechnicalMessage(String technicalMessage) {
        this.technicalMessage = technicalMessage;
    }

    @Override public String toString() {
        return "ErrorResponse{" +
                "errorMessage='" + errorMessage + '\'' +
                ", technicalMessage='" + technicalMessage + '\'' +
                '}';
    }
}
