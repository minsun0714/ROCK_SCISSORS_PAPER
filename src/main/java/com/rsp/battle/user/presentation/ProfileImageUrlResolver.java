package com.rsp.battle.user.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProfileImageUrlResolver {

    @Value("${cloud.aws.s3.public-url}")
    private String baseUrl;

    public String resolve(String key) {
        if (key == null) return null;
        return baseUrl + "/" + key;
    }
}