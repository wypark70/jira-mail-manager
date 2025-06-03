package com.samsungds.ims.mail.component;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SmtpMessageHandlerTest {

    @Test
    void testSearchUser() {
        try {
            HttpResponse<JsonNode> response = Unirest.get("http://localhost:2990/jira/rest/api/2/user/search")
                    .basicAuth("admin", "admin")
                    .queryString("username", "admin")
                    .asJson();
            if (response.getStatus() == 200) {
                JSONArray users = response.getBody().getArray();
                assertNotEquals(0, users.length());
                assertEquals("admin@samsung.com", users.getJSONObject(0).getString("emailAddress"));
                users.forEach(user -> log.info("user: {}", user.toString()));
            }
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

}