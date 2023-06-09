package com.bogdan.video.microservice.service;

import com.bogdan.video.microservice.exception.VideoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UtilityService {

    @Value("${service.url}")
    private String serviceUrl;

    private final RestTemplate restTemplate;

    public String getEmailFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public Integer getIdFromAccountMicroservice(final String email) {
        ResponseEntity<Integer> response = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            response = restTemplate.postForEntity(serviceUrl + "/videoplatform/api/account/getIdByEmail", request, Integer.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

    public String getChannelNameByUserId(final Long userId) {
        String url = serviceUrl + "/videoplatform/api/account/channelNameById/" + userId;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new VideoException("Nu am gasit user-ul");
        }

        return response.getBody();
    }

    public Long getIdByChannelName(final String channelName) {
        String url = serviceUrl + "/videoplatform/api/account/getIdByChannelName/" + channelName;
        try {
            ResponseEntity<Long> response = restTemplate.getForEntity(url, Long.class);
            return response.getBody();
        } catch (Exception e) {
            throw new VideoException(e.getMessage());
        }
    }

}
