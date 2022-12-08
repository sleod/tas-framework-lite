package ch.qa.testautomation.framework.http;

import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class SimpleHttpClient {

    public static int timeout = 30;

    /**
     * @param params  map of parameters
     * @param command command like POST, GET...
     */
    public static String sendRequest(String path, Map<String, String> params, String command, String payload) {
        if (!params.isEmpty()) {
            path += getParamsString(params);
        }
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
        try {
            HttpResponse<String> response = client.send(getHttpRequest(command, path, payload), HttpResponse.BodyHandlers.ofString());
            return getResponseString(response);
        } catch (IOException | InterruptedException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, ex, "Failed on send request: " + command + " " + path);
        }
    }

    private static HttpRequest getHttpRequest(String command, String path, String payload) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(path))
                .timeout(Duration.ofSeconds(timeout))
                .header("Content-Type", "application/json");
        return switch (command.toUpperCase()) {
            case "GET" -> builder.GET().build();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(payload)).build();
            case "DELETE" -> builder.DELETE().build();
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(payload)).build();
            default ->
                    throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE,
                            "Failed on get Request with Command: " + command + "! Supported are: GET, DELETE, PUT and POST.");
        };
    }

    private static String getParamsString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        result.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append("&");
        }
        return result.substring(0, result.length() - 1);
    }

    private static String getResponseString(HttpResponse<String> response) {
        return response.toString() + "\n" + response.body();
    }

}
