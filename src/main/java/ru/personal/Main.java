package ru.personal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    private static final String ADDRESS_A = "Екатеринбург, улица Мира 33";
    private static final String ADDRESS_B = "Екатеринбург, улица Шефская 108";

    private static final String API_KEY = "5b3ce3597851110001cf6248269b19a89dfc4f20970575c9fcfd37db";
    private static final String ORS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String OSM_SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    private static final String OSM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";

    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws URISyntaxException, IOException {
        String locationAJson
                = doGetRequest(OSM_SEARCH_URL, Map.of(
                "q", ADDRESS_A,
                "format", "json"
        ));

        final Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
        final List<Map<String, Object>> parsedLocationA = GSON.fromJson(locationAJson, type);
        final Map<String, Object> addressAInfo = parsedLocationA.get(0);
        String coordinatesA = addressAInfo.get("lon").toString() + "," + addressAInfo.get("lat").toString();
        System.out.println(coordinatesA);

        String locationBJson = doGetRequest(OSM_SEARCH_URL, Map.of(
                "q", ADDRESS_B,
                "format", "json"
        ));
        final List<Map<String, Object>> parsedLocationB = GSON.fromJson(locationBJson, type);
        final Map<String, Object> addressBInfo = parsedLocationB.get(0);
        String coordinatesB = addressBInfo.get("lon").toString() + "," + addressBInfo.get("lat").toString();
        System.out.println(coordinatesB);

        String routeJson = doGetRequest(ORS_URL, Map.of(
                "api_key", API_KEY,
                "start", coordinatesA,
                "end", coordinatesB
        ));
        System.out.println(routeJson);
        final JsonObject routeInfo = GSON.fromJson(routeJson, JsonObject.class);
        System.out.println(routeInfo);
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