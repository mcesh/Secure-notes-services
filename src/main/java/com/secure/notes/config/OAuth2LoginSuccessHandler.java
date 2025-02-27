package com.secure.notes.config;

import com.secure.notes.entities.Role;
import com.secure.notes.entities.UserEntity;
import com.secure.notes.enums.AppRole;
import com.secure.notes.repositories.RoleRepository;
import com.secure.notes.security.jwt.JwtUtils;
import com.secure.notes.security.services.UserDetailsImpl;
import com.secure.notes.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserService userService;

    private final JwtUtils jwtUtils;

    private final RoleRepository roleRepository;

    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.front-end.url}")
    private String frontendUrl;

    String username;
    String idAttributeKey;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        if ("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()) || "google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
            DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = principal.getAttributes();
            String email;
            if ("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())){
                email = getEmailFromGitHub(oAuth2AuthenticationToken);
            }else {
                email = attributes.getOrDefault("email", "").toString();
                String name = attributes.getOrDefault("name", "").toString();
            }

            if ("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
                assert email != null;
                username = email.split("@")[0];
                idAttributeKey = "id";
            } else if ("google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
                assert email != null;
                username = email.split("@")[0];
                idAttributeKey = "sub";
            } else {
                username = "";
                idAttributeKey = "id";
            }

            /** @Author: Siya Nxuseka
             * @Description: manage OAuth2 login by authenticating users, redirect locked account and register new users
             * */
            userService.findByEmail(email)
                    .ifPresentOrElse(user -> {
                        if (!user.isAccountNonLocked()) {
                            String lockedRedirectUrl = frontendUrl + "/access-denied"; // TODO implement React relevant page and change this URI path
                            try {
                                getRedirectStrategy().sendRedirect(request, response, lockedRedirectUrl);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }
                        DefaultOAuth2User oauthUser = new DefaultOAuth2User(
                                List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())),
                                attributes,
                                idAttributeKey
                        );
                        Authentication securityAuth = new OAuth2AuthenticationToken(
                                oauthUser,
                                List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
                        );
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    }, () -> {
                        UserEntity newUser = new UserEntity();
                        Optional<Role> userRole = roleRepository.findByRoleName(AppRole.ROLE_USER); // Fetch existing role
                        if (userRole.isPresent()) {
                            newUser.setRole(userRole.get()); // Set existing role
                        } else {
                            // Handle the case where the role is not found
                            throw new RuntimeException("Default role not found");
                        }
                        newUser.setEmail(email);
                        newUser.setUserName(username);
                        newUser.setSignUpMethod(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        userService.registerUser(newUser);
                        DefaultOAuth2User oauthUser = new DefaultOAuth2User(
                                List.of(new SimpleGrantedAuthority(newUser.getRole().getRoleName().name())),
                                attributes,
                                idAttributeKey
                        );
                        Authentication securityAuth = new OAuth2AuthenticationToken(
                                oauthUser,
                                List.of(new SimpleGrantedAuthority(newUser.getRole().getRoleName().name())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
                        );
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    });
        }
        this.setAlwaysUseDefaultTargetUrl(true);

        // JWT TOKEN LOGIC
        DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        // Extract necessary attributes
        String email;
        if ("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())){
            email = getEmailFromGitHub(oAuth2AuthenticationToken);
        }else {
            email = (String) attributes.get("email");
        }

        System.out.println("OAuth2LoginSuccessHandler: " + username + " : " + email); // TODO remove this code

        Set<SimpleGrantedAuthority> authorities = new HashSet<>(oauth2User.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .collect(Collectors.toList()));
        UserEntity user = userService.findByEmail(email).orElseThrow(
                () -> new RuntimeException("User not found"));
        authorities.add(new SimpleGrantedAuthority(user.getRole().getRoleName().name()));

        // Create UserDetailsImpl instance
        UserDetailsImpl userDetails = new UserDetailsImpl(
                null,
                username,
                email,
                null,
                false,
                authorities
        );

        // Generate JWT token
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        // Redirect to the frontend with the JWT token
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", jwtToken)
                .build().toUriString();
        this.setDefaultTargetUrl(targetUrl);
        super.onAuthenticationSuccess(request, response, authentication);

    }

    private String getEmailFromGitHub(OAuth2AuthenticationToken token) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(),
                token.getName());
        OAuth2AccessToken accessToken = client.getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken.getTokenValue());
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                });

        List<Map<String, Object>> emails = response.getBody();
        if (emails != null && !emails.isEmpty()) {
            for (Map<String, Object> email : emails) {
                if (Boolean.TRUE.equals(email.get("primary"))) {
                    return email.get("email").toString();
                }
            }
        }
        return null;
    }
}
