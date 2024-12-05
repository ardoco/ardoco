/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.textprocessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class HttpCommunicator {

    public static final String ENV_USERNAME = "SCNLP_SERVICE_USER";
    public static final String ENV_PASSWORD = "SCNLP_SERVICE_PASSWORD";

    public String sendAuthenticatedGetRequest(String requestUrl) throws IOException {
        String username = System.getenv(ENV_USERNAME);
        String password = System.getenv(ENV_PASSWORD);
        if (username == null || password == null) {
            throw new IOException("Environment variables " + ENV_USERNAME + " and " + ENV_PASSWORD + " must be set.");
        }

        HttpGet request = new HttpGet(requestUrl);
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(username, password.toCharArray()));
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build()) {
            return httpClient.execute(request, new BasicHttpClientResponseHandler());
        }
    }

    public String sendAuthenticatedPostRequest(String requestUrl, String body) throws IOException {
        String username = System.getenv(ENV_USERNAME);
        String password = System.getenv(ENV_PASSWORD);
        if (username == null || password == null) {
            throw new IOException("Environment variables " + ENV_USERNAME + " and " + ENV_PASSWORD + " must be set.");
        }

        HttpPost request = new HttpPost(requestUrl);
        StringEntity requestEntity = new StringEntity(body, ContentType.APPLICATION_JSON, StandardCharsets.UTF_8.toString(), false);
        request.setEntity(requestEntity);

        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(username, password.toCharArray()));
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build()) {
            return httpClient.execute(request, new BasicHttpClientResponseHandler());
        }
    }
}
