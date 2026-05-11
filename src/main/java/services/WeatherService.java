package services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherService {
    private static final String API_KEY = "b12210e3457ac4b6f69e8e4dc9e02ae8";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final OkHttpClient client = new OkHttpClient();

    public static String getWeatherForecast(String city, LocalDateTime eventDate) {
        if (city == null || city.trim().isEmpty()) return "Lieu non spécifié";
        city = city.trim();

        long eventEpoch = eventDate.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        long nowEpoch = LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();

        if (eventEpoch < nowEpoch - 3600 * 24) { // More than 1 day in the past
            return "Événement déjà passé";
        }
        if (eventEpoch > nowEpoch + (5 * 24 * 3600)) { // More than 5 days
            return "Météo disponible à 5 jours max";
        }

        okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(BASE_URL).newBuilder()
                .addQueryParameter("q", city)
                .addQueryParameter("appid", API_KEY)
                .addQueryParameter("units", "metric")
                .addQueryParameter("lang", "fr")
                .build();
        
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String jsonData = response.body().string();
            if (!response.isSuccessful()) {
                System.err.println("Weather API Error: " + response.code() + " - " + jsonData);
                if (response.code() == 404) {
                    return "Ville non trouvée (" + city + ")";
                }
                if (response.code() == 401) {
                    return "Clé API non activée/invalide";
                }
                return "Météo non disponible (Erreur " + response.code() + ")";
            }

            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray list = jsonObject.getJSONArray("list");

            if (list.length() == 0) {
                return "Aucune donnée reçue";
            }

            // OpenWeatherMap forecast provides data every 3 hours for 5 days.
            // We look for the forecast closest to our event date.
            JSONObject closestForecast = null;
            long minDiff = Long.MAX_VALUE;

            for (int i = 0; i < list.length(); i++) {
                JSONObject forecast = list.getJSONObject(i);
                long forecastEpoch = forecast.getLong("dt");
                long diff = Math.abs(eventEpoch - forecastEpoch);

                if (diff < minDiff) {
                    minDiff = diff;
                    closestForecast = forecast;
                }
            }

            if (closestForecast != null) {
                String description = closestForecast.getJSONArray("weather").getJSONObject(0).getString("description");
                double temp = closestForecast.getJSONObject("main").getDouble("temp");
                
                // Capitalize first letter
                description = description.substring(0, 1).toUpperCase() + description.substring(1);
                
                return description + " (" + Math.round(temp) + "°C)";
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error parsing weather data: " + e.getMessage());
        }

        return "Météo non disponible pour cette date";
    }
}
