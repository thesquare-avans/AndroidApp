package me.thesquare.models;

/**
 * Created by ruben on 14-6-2017.
 */

class ErrorModel {

    private String code;

    public ErrorModel(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
