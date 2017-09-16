package com.sharefile.sdk.controller.util;

import com.google.gson.JsonObject;


public class OAuth2Token {
    private String accessToken = "";
    private String refreshToken = "";
    private String tokenType = "";
    private String appcp = "";
    private String apicp = "";
    private String subdomain = "";


    public String getSubdomain() {
        return subdomain;
    }


    private int expiresIn = 0;

    public String getAccessToken() {
        return accessToken;
    }



    public OAuth2Token(JsonObject json) {
        if (json != null) {
            accessToken =(json.get("access_token").getAsString());
            refreshToken = String.valueOf(json.get("refresh_token").getAsString());
            tokenType = String.valueOf(json.get("token_type").getAsString());
            appcp = String.valueOf(json.get("appcp").getAsString());
            subdomain = String.valueOf(json.get("subdomain").getAsString());
            expiresIn = json.get("expires_in").getAsInt();
        }
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

}
