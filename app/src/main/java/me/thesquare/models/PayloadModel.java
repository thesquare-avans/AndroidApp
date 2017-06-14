package me.thesquare.models;

/**
 * Created by ruben on 14-6-2017.
 */

public class PayloadModel {

    private boolean success;

    private ErrorModel error;

    public PayloadModel(boolean success, ErrorModel error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ErrorModel getError() {
        return error;
    }

    public void setError(ErrorModel error) {
        this.error = error;
    }
}
