package example.httpclient;

import ch.qa.testautomation.framework.common.logging.SystemLogger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SimpleRestClient {
    public static final String CONTENT_TYPE = "application/json";

    /**
     * @param config  with host data and settings
     * @param params  map of parameters
     * @param command command like POST, GET...
     * @throws IOException io exception
     */
    public static String sendRequest(Map<String, String> config, Map<String, String> params, String command, String path, String payload, String contentType) throws IOException {
        String endpoint = config.get("host") + path;
        if (!params.isEmpty()) {
            endpoint += getParamsString(params);
        }
        URL url = new URL(endpoint);
        //creates a connection object but doesn't establish the connection yet
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(true);
        con.setRequestMethod(command);
        con.setRequestProperty("Content-Type", contentType);
        con.setRequestProperty("Accept", contentType);
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        if (!payload.isEmpty()) {
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(payload);
            out.flush();
            out.close();
        }

        // normally, 3xx is redirect
        int status = con.getResponseCode();
        if ((status == HttpsURLConnection.HTTP_MOVED_TEMP
                || status == HttpsURLConnection.HTTP_MOVED_PERM
                || status == HttpsURLConnection.HTTP_SEE_OTHER
                || status == 308)) {
            SystemLogger.warn("Response Code: " + status);
            // get redirect url from "location" header field
            String newUrl = con.getHeaderField("Location");
            // open the new connection again
            con = (HttpsURLConnection) new URL(newUrl).openConnection();
        }

        //To execute the request, we can use the getResponseCode(), connect(), getInputStream() or getOutputStream() methods
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        response.append("Code: ").append(con.getResponseCode()).append("\n").append("Content:\n");
        String responseLine;
        while ((responseLine = reader.readLine()) != null) {
            response.append(responseLine.trim()).append("\n");
        }
        reader.close();
        con.disconnect();
        return response.toString();
    }

    private static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        result.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                    .append("&");
        }
        return result.substring(0, result.length() - 1);
    }


//    @Test
//    public void testClient() throws IOException {
//        Map<String, String> config = new HashMap<>(6);
//        Map<String, String> params = new HashMap<>(6);
//        String payload = "{\n" +
//                "  \"results\": [\n" +
//                "    {\n" +
//                "      \"content_base64\": \"Y29udGVudCBleGFtcGxlMQ==\",\n" +
//                "      \"file_name\": \"example1\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"content_base64\": \"Y29udGVudCBleGFtcGxlMg==\",\n" +
//                "      \"file_name\": \"example2\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"content_base64\": \"Y29udGVudCBleGFtcGxlMw==\",\n" +
//                "      \"file_name\": \"example3\"\n" +
//                "    }\n" +
//                "  ]\n" +
//                "}";
//
//        config.put("host", "https://vlx01193.server.raiffeisen.ch/allure/allure-docker-service");
//        config.put("project_id", "framework-dev-feature-test");
//        config.put("force_project_creation", "true");
//        config.put("execution_name", "Automated Test Demo");
//        config.put("execution_from", "Xin Lu");
//        config.put("execution_type", "Bamboo");
//        params.put("project_id", config.get("project_id"));
//        params.put("force_project_creation", config.get("force_project_creation"));
////        String response = sendRequest(config, params, "POST", "/send-results", payload);
//        String response = sendRequest(config, Collections.emptyMap(), "GET", "/projects", "", CONTENT_TYPE);
//        SystemLogger.info("Rest Response:" + "\n" + response);
//    }
}
