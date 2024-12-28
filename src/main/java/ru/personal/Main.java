package ru.personal;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String API_KEY = "5b3ce3597851110001cf6248269b19a89dfc4f20970575c9fcfd37db";
    private static final String COORDINATES_A = "60.622083893794525,56.90695817776867";
    private static final String COORDINATES_B = "60.65373517379274,56.83601410312533";
    private static final String ADDRESS_A = "Екатеринбург, улица Мира 33";
    private static final String ORS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String OSM_SEARCH_URL = "https://nominatim.openstreetmap.org/search";

    public static void main(String[] args) throws URISyntaxException, IOException {
        String response = doGetRequest(ORS_URL, Map.of(
                "api_key", API_KEY,
                "start", COORDINATES_A,
                "end", COORDINATES_B
        ));
        System.out.println(response);

        String searchResponse = doGetRequest(OSM_SEARCH_URL, Map.of(
                "q", ADDRESS_A,
                "format", "json"
        ));
        System.out.println(searchResponse);
    }

    static String doGetRequest(final String url, final Map<String, String> queryParams) throws IOException, URISyntaxException {
        final String paramString = buildQueryParams(queryParams);

        final HttpURLConnection connection = (HttpURLConnection) new URI(url + paramString).toURL().openConnection();
        connection.setRequestMethod("GET");
        System.out.println(connection.getResponseCode());
        System.out.println(connection.getResponseMessage());

        final Scanner scanner = new Scanner(connection.getInputStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());

        }
        return response.toString();
    }

    static String buildQueryParams(final Map<String, String> queryParams) {
        if(queryParams.isEmpty()) {
            return "";
        }
        List<String> formattedParams = new ArrayList<>();
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            formattedParams.add(String.format("%s=%s", param.getKey(), URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8)));
        }
        final StringBuilder stringBuilder = new StringBuilder("?");
        for(int i = 0; i < formattedParams.size(); i++) {
            final String param = formattedParams.get(i);
            stringBuilder.append(i != 0 ? "&" : "").append(param);
        }
        return stringBuilder.toString();
    }
}