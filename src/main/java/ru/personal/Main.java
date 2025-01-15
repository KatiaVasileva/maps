package ru.personal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
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
        // Получение координат по адресу
        String coordinatesA = getCoordinates(ADDRESS_A);
        String coordinatesB = getCoordinates(ADDRESS_B);

        // Запрос GET для получения маршрута по указанным координатам исходной и конечной точек
        String routeJson = doGetRequest(ORS_URL, Map.of(
                "api_key", API_KEY,
                "start", coordinatesA,
                "end", coordinatesB
        ));
        // Обработка geojson для извлечения из него информации в полях distance и duration
        final JsonObject routeInfo = GSON.fromJson(routeJson, JsonObject.class);
        final JsonObject routeSegment = routeInfo.getAsJsonArray("features")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("properties")
                .getAsJsonArray("segments")
                .get(0)
                .getAsJsonObject();
        System.out.println(routeSegment.getAsJsonPrimitive("distance"));
        System.out.println(routeSegment.getAsJsonPrimitive("duration"));

        // Обработка geojson для извлечения из него информации в поле steps
        for (JsonElement step : routeSegment.getAsJsonArray("steps")) {
            System.out.println(step.getAsJsonObject().getAsJsonPrimitive("duration").getAsString());
            System.out.println(step.getAsJsonObject().getAsJsonPrimitive("distance").getAsString());
            System.out.println(step.getAsJsonObject().getAsJsonPrimitive("instruction").getAsString());
        }
    }

    // Метод для создания соединения и выполнения GET-запроса, который возвращает ответ в виде строки
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

    // Метод для создания строки запроса с нужными параметрами
    static String buildQueryParams(final Map<String, String> queryParams) {
        if (queryParams.isEmpty()) {
            return "";
        }
        List<String> formattedParams = new ArrayList<>();
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            formattedParams.add(String.format("%s=%s", param.getKey(), URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8)));
        }
        final StringBuilder stringBuilder = new StringBuilder("?");
        for (int i = 0; i < formattedParams.size(); i++) {
            final String param = formattedParams.get(i);
            stringBuilder.append(i != 0 ? "&" : "").append(param);
        }
        return stringBuilder.toString();
    }

    // Метод для выполнения GET-запроса, который принимает адрес и возвращает координаты этого адреса в виде строки
    static String getCoordinates(String address) throws IOException, URISyntaxException {
        String locationJson = doGetRequest(OSM_SEARCH_URL, Map.of(
                "q", address,
                "format", "json"
        ));
        // Обработка полученного json для извлечения из него полей "lon" (долгота) и "lat" (широта)
        final JsonArray parsedLocation = GSON.fromJson(locationJson, JsonArray.class);
        final JsonObject addressInfo = parsedLocation.get(0).getAsJsonObject();
        String coordinates = addressInfo.getAsJsonPrimitive("lon").getAsString()
                + "," + addressInfo.getAsJsonPrimitive("lat").getAsString();
        return coordinates;
    }
}