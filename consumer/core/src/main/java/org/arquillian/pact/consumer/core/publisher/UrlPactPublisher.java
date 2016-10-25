package org.arquillian.pact.consumer.core.publisher;


import org.arquillian.pact.common.configuration.PactRunnerExpressionParser;
import org.arquillian.pact.consumer.spi.publisher.PactPublisher;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class UrlPactPublisher implements PactPublisher {

    private static final Logger logger = Logger.getLogger(UrlPactPublisher.class.getName());
    private static final String URL = "url";
    private static final String USER_AGENT = "Mozilla/5.0";

    private Map<String, Object> configuration = null;

    @Override
    public void store(Path pactsLocation) {

        String url = PactRunnerExpressionParser.parseExpressions((String) this.configuration.get(URL));

        try (Stream<Path> stream = Files.walk(pactsLocation)) {
            stream.forEach(path -> {
                try {
                    if (! Files.isDirectory(path)) {
                        URL fullUrl = new URL(url + '/' +path.getFileName().toString());
                        sendPost(fullUrl, path);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public void sendPost(URL url, Path content) throws IOException {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "UTF-8");

            con.setDoOutput(true);

            try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(con.getOutputStream())) {
                Files.copy(content, bufferedOutputStream);
            }

            int responseCode = con.getResponseCode();
            StringBuilder response = readResponse(con);
            if (isFailureResponseCode(responseCode)) {
                throw new IOException(String.format("Http Post failed with status code %s and error message %s", responseCode, response.toString()));
            }
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    protected StringBuilder readResponse(HttpURLConnection con) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return response;
    }

    protected boolean isFailureResponseCode(int responseCode) {
        return responseCode < 200 && responseCode > 299;
    }

    @Override
    public String getName() {
        return "url";
    }

    @Override
    public void configure(Map<String, Object> configuration) {

        this.configuration = configuration;

        if (!this.configuration.containsKey(URL)) {
            throw new IllegalArgumentException(String.format("Url Pact Publisher requires %s configuration property", URL));
        }

        if (!(this.configuration.get(URL) instanceof String)) {
            throw new IllegalArgumentException(String.format("Url Pact Publisher requires %s configuration property to be an String", URL));
        }

    }
}
