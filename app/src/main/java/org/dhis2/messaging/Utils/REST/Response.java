package org.dhis2.messaging.Utils.REST;

/**
 * Created by iNick on 22.09.14.
 */
public class Response {
    private final int code;
    private final String body;
    public Response(int code, String body) {
        this.code = code;
        this.body = body;
    }
    public int getCode() {
        return code;
    }
    public String getBody() {
        return body;
    }
}