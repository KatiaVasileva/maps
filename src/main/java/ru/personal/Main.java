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
    private static final String API_KEY = "5b3ce3597851110001cf6248269b19a89dfc4f20970575c9fcfd37db";
    private static final String ORS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String OSM_SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    private static final String OSM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";

    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws URISyntaxException, IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите начальный адрес: ");
        String addressA = scanner.nextLine();

        System.out.println("Введите конечный адрес: ");
        String addressB = scanner.nextLine();

        // Получение координат по адресу
        String coordinatesA = "";
        String coordinatesB = "";

        try {
            coordinatesA = getCoordinates(addressA);
            coordinatesB = getCoordinates(addressB);
        } catch (Exception e) {
            System.out.println("Адрес не найден");
        }

        // Запрос GET для получения маршрута по указанным координатам исходной и конечной точек
        // Обработка полученного JsonElement для извлечения из него информации в полях distance и duration
        JsonObject routeSegment = doGetRequest(ORS_URL, Map.of(
                "api_key", API_KEY,
                "start", coordinatesA,
                "end", coordinatesB
        ))
                .getAsJsonObject()
                .getAsJsonArray("features")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("properties")
                .getAsJsonArray("segments")
                .get(0)
                .getAsJsonObject();
        int distance = routeSegment.getAsJsonPrimitive("distance").getAsInt();
        int duration = routeSegment.getAsJsonPrimitive("duration").getAsInt();

        System.out.println(formatDistance(distance));
        System.out.println(formatTime(duration));

        // Обработка JsonObject routeSegment для извлечения из него информации в поле steps
        // Структура routeSegment:
        // {
        //    "distance":10566.9,
        //    "duration":976.2,
        //    "steps":[
        //        {"distance":54.7,
        //        "duration":13.1,
        //        "type":11,
        //        "instruction":"Head south","name":"-",
        //        "way_points":[0,3]}
        //     ]
        //  }

        for (JsonElement step : routeSegment.getAsJsonArray("steps")) {
            int stepDuration = step.getAsJsonObject().getAsJsonPrimitive("duration").getAsInt();
            int stepDistance = step.getAsJsonObject().getAsJsonPrimitive("distance").getAsInt();
            String stepInstruction = step.getAsJsonObject().getAsJsonPrimitive("instruction").getAsString();
            System.out.println("-------------------------------------");

            System.out.println(formatTime(stepDuration));
            System.out.println(formatDistance(stepDistance));
            System.out.println("Маршрут: " + stepInstruction);
        }
    }

    // Метод для создания соединения и выполнения GET-запроса, который возвращает ответ в виде строки
    static JsonElement doGetRequest(final String url, final Map<String, String> queryParams) throws IOException, URISyntaxException {
        final String paramString = buildQueryParams(queryParams);

        final HttpURLConnection connection = (HttpURLConnection) new URI(url + paramString).toURL().openConnection();
        connection.setRequestMethod("GET");
        try {
            final Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            return GSON.fromJson(response.toString(), JsonElement.class);
        } catch (IOException e) {
            System.out.println("Введен неправильный или несуществующий адрес");
            throw e;
        }
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
    static String getCoordinates(String address) throws Exception {
        JsonArray addressInfoArray = doGetRequest(OSM_SEARCH_URL, Map.of(
                "q", address,
                "format", "json"
        ))
                .getAsJsonArray();
        if (addressInfoArray.isEmpty()) {
            throw new Exception("Адрес не найден");
        } else {
            JsonObject addressInfo = addressInfoArray
                    .get(0)
                    .getAsJsonObject();

            return addressInfo.getAsJsonPrimitive("lon").getAsString()
                    + "," + addressInfo.getAsJsonPrimitive("lat").getAsString();
        }
    }

    // Метод для форматирования полученного значения времени
    static String formatTime(int time) {
        String formattedTime;
        if (time % 3600 / 60 == 0) {
            formattedTime = "Продолжительность шага: " + time + " c ";
        } else if (time / 3600 == 0) {
            formattedTime = "Продолжительность шага: " + (time % 3600 / 60) + " мин ";
        } else {
            formattedTime = "Продолжительность маршрута: " + time / 3600 + " ч " + (time % 3600 / 60) + " мин";
        }
        return formattedTime;
    }

    // Метод для форматирования полученного значения расстояния
    static String formatDistance(int distance) {
        String formattedDistance;
        if (distance / 1000 == 0) {
            formattedDistance = "Расстояние: " + distance + " м ";
        } else {
            formattedDistance = "Расстояние: " + String.format("%.1f", (float) distance / 1000) + " км ";
        }
        return formattedDistance;
    }
}