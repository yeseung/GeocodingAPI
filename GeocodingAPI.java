import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gongdaeoppa.moongom.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeocodingService {
    
    private static String GOOGLE_API_KEY;
    
    public static void init(String googleApiKey) {
        GeocodingService.GOOGLE_API_KEY = googleApiKey;
    }
    
    public Map<String, Object> getLatLng(String address) {
        
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?key=" + GOOGLE_API_KEY + "&address=" + Util.getUrlEncoded(address));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            
            connection.getLastModified();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            try {
                Map<String, Object> responseMap = new ObjectMapper().readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
                if ("OK".equals(responseMap.get("status"))) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) responseMap.get("results");
                    if (results.isEmpty()) {
                        throw new RuntimeException("No results found for the given address");
                    }
                    Map<String, Object> geometry = (Map<String, Object>) results.get(0).get("geometry");
                    Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                    return location;
                } else {
                    throw new RuntimeException("Error from the API: " + responseMap.get("status"));
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse response", e);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
    

}
