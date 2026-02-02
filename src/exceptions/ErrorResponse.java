package exceptions;

public class ErrorResponse {

    private String errorMessage;
    private Integer errorCode;
    private String urlOfRequest;

    public ErrorResponse(String urlOfRequest, String errorMessage, Integer errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.urlOfRequest = urlOfRequest;
    }

    public String getUrlOfRequest() {
        return urlOfRequest;
    }

    public void setUrlOfRequest(String urlOfRequest) {
        this.urlOfRequest = urlOfRequest;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
