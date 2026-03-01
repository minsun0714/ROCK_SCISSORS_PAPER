package com.rsp.battle.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2StateRedisService oAuth2StateRedisService;
    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(OAuth2StateRedisService oAuth2StateRedisService, ClientRegistrationRepository repo) {
        this.oAuth2StateRedisService = oAuth2StateRedisService;
        this.defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customize(req, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customize(req, request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req,
                                                 HttpServletRequest request) {
        if (req == null) return null;

        String appRedirectUri = request.getParameter("app_redirect_uri");
        log.info("uri={}, query={}, appRedirectUri={}",
                request.getRequestURI(),
                request.getQueryString(),
                appRedirectUri);
        String state = req.getState();
        oAuth2StateRedisService.saveRedirectUri(state, appRedirectUri);

        return OAuth2AuthorizationRequest.from(req)
                .attributes(attrs -> attrs.put("app_redirect_uri", appRedirectUri))
                .build();
    }
}
