package com.calclone.handler;

import com.calclone.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository;


    public OAuth2SuccessHandler(OAuth2AuthorizedClientService authorizedClientService,
                                UserRepository userRepository) {
        this.authorizedClientService = authorizedClientService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );


        String accessToken = client.getAccessToken().getTokenValue();

        request.getSession().setAttribute("googleAccessToken", accessToken);

//        super.onAuthenticationSuccess(request, response, authentication);
        String email = oauthToken.getPrincipal().getAttribute("email");
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setGoogleAccessToken(accessToken);
            userRepository.save(user);

            System.out.println("Token saved for user: " + email);
            System.out.println("Token preview: " + accessToken.substring(0, 20) + "...");
        });

        response.sendRedirect("/events");
    }
}