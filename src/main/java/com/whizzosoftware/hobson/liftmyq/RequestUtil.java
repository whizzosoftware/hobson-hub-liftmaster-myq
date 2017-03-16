package com.whizzosoftware.hobson.liftmyq;

import java.util.HashMap;
import java.util.Map;

public class RequestUtil {
    private static final String APP_ID = "JVM/G9Nwih5BwKgNCjLxiFUQxQijAebyyg8QUHr7JOrP+tuPb8iHfRHKwTmDzHOu";
    private static final String CULTURE = "en";

    static public Map<String,String> createHeaders() {
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("User-Agent", "Chamberlain/3.73");
        headers.put("BrandID", "2");
        headers.put("ApiVersion", "4.1");
        headers.put("Culture", CULTURE);
        headers.put("MyQApplicationID", APP_ID);
        return headers;
    }
}
