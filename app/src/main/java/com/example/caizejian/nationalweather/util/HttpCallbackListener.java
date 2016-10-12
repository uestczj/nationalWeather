package com.example.caizejian.nationalweather.util;

/**
 * Created by caizejian on 16/10/12.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
