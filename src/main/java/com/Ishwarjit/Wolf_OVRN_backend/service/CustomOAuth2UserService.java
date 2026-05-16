package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.entity.User;
import com.Ishwarjit.Wolf_OVRN_backend.entity.UserRole;
import com.Ishwarjit.Wolf_OVRN_backend.repository.UserRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    public static final String ATTR_USER_ID = "appUserId";

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerId = Objects.toString(attributes.get("sub"), null);
        String email = Objects.toString(attributes.get("email"), null);
        String givenName = Objects.toString(attributes.get("given_name"), null);
        String familyName = Objects.toString(attributes.get("family_name"), null);
        String imageUrl = Objects.toString(attributes.get("picture"), null);

        if (providerId == null || email == null) {
            throw new OAuth2AuthenticationException("Missing sub or email claim from provider");
        }

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .map(existing -> {
                    boolean changed = false;
                    if (!Objects.equals(existing.getEmail(), email)) {
                        existing.setEmail(email);
                        changed = true;
                    }
                    if (!Objects.equals(existing.getFirstName(), givenName)) {
                        existing.setFirstName(givenName);
                        changed = true;
                    }
                    if (!Objects.equals(existing.getLastName(), familyName)) {
                        existing.setLastName(familyName);
                        changed = true;
                    }
                    if (!Objects.equals(existing.getImageUrl(), imageUrl)) {
                        existing.setImageUrl(imageUrl);
                        changed = true;
                    }
                    return changed ? userRepository.save(existing) : existing;
                })
                .orElseGet(() -> {
                    User created = new User();
                    created.setEmail(email);
                    created.setFirstName(givenName);
                    created.setLastName(familyName);
                    created.setImageUrl(imageUrl);
                    created.setProvider(provider);
                    created.setProviderId(providerId);
                    created.setRole(UserRole.USER);
                    return userRepository.save(created);
                });

        Map<String, Object> enriched = new HashMap<>(attributes);
        enriched.put(ATTR_USER_ID, user.getId().toString());
        enriched.put("role", user.getRole().name());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                enriched,
                "sub");
    }
}
