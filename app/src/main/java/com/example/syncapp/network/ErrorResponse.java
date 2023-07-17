package com.example.syncapp.network;

@SuppressWarnings("unused")
public class ErrorResponse {
    private String message;
    private boolean canRetry;
    private boolean isUserFriendly;

    public ErrorResponse() {
    }

    public ErrorResponse(String message, boolean canRetry, boolean isUserFriendly) {
        this.message = message;
        this.canRetry = canRetry;
        this.isUserFriendly = isUserFriendly;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCanRetry() {
        return canRetry;
    }

    public void setCanRetry(boolean canRetry) {
        this.canRetry = canRetry;
    }

    public boolean isUserFriendly() {
        return isUserFriendly;
    }

    public void setUserFriendly(boolean userFriendly) {
        isUserFriendly = userFriendly;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "message='" + message + '\'' +
                ", canRetry=" + canRetry +
                ", isUserFriendly=" + isUserFriendly +
                '}';
    }
}
