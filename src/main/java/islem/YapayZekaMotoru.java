package islem;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class YapayZekaMotoru {

    // GÜVENLİK DÜZELTMESİ: API anahtarını Environment Variable (Çevresel Değişken) olarak alıyoruz.
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");

    // Model adını 2.0-flash olarak güncel tutabilirsin veya 2.5-flash olarak bırakabilirsin.
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public static class ZekaCiktisi {
        public String ilk_kelimeler;
        public String son_kelimeler;
        public int puan;
    }

    public List<ZekaCiktisi> analizEt(String srtYolu) {
        System.out.println("Yapay Zeka, SRT altyazı dosyasını okuyor: " + srtYolu);
        List<ZekaCiktisi> sonuclar = new ArrayList<>();

        if (API_KEY == null || API_KEY.isEmpty()) {
            System.out.println("ÖLÜMCÜL HATA: GEMINI_API_KEY çevresel değişkeni bulunamadı! Lütfen IntelliJ Run ayarlarından ekleyin.");
            return sonuclar;
        }

        try {
            String srtMetni = Files.readString(Path.of(srtYolu));

            String sistemEmri = "Sen milyonlarca izlenen YouTube Shorts kurgulayan usta bir yönetmensin. " +
                    "Aşağıda sana bir videonun SRT altyazı metnini vereceğim. Görevin, viral potansiyeli en yüksek, kesintisiz ve anlam bütünlüğü olan 30-50 saniyelik 1 kesit bulmak. " +
                    "BANA SANİYE VERMEYECEKSİN. Bana sadece bu klibin tam olarak hangi cümleyle (ilk 5-6 kelime) başlayacağını ve tam olarak hangi cümleyle (son 5-6 kelime) biteceğini söyleyeceksin. " +
                    "Seçtiğin kelimeler altyazı metninde BİREBİR aynı olmak zorundadır. Noktalama işaretlerine kadar. " +
                    "Çıktıyı SADECE şu JSON formatında ver, kod bloğu veya başka hiçbir şey yazma: " +
                    "[ {\"ilk_kelimeler\": \"Bunun sebebi bilimsel olarak kanıtlanmış\", \"son_kelimeler\": \"kara delikler bu yüzden ışıksızdır.\", \"puan\": 99} ]\n\n" +
                    "İşte Altyazı Metni:\n" + srtMetni;

            String guvenliEmir = sistemEmri.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + guvenliEmir + "\"}]}]}";

            HttpClient postaci = HttpClient.newHttpClient();
            HttpRequest istek = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            System.out.println("İstem Mühendisliği hazırlanıyor ve Gemini'a gönderiliyor...");

            int deneme = 0;
            boolean basarili = false;
            HttpResponse<String> cevap = null;

            while (deneme < 3 && !basarili) {
                cevap = postaci.send(istek, HttpResponse.BodyHandlers.ofString());
                if (cevap.statusCode() == 200) {
                    basarili = true;
                } else {
                    deneme++;
                    System.out.println("API Hatası (" + cevap.statusCode() + "). Deneme: " + deneme + "/3");
                    Thread.sleep(5000);
                }
            }

            if (basarili && cevap != null) {
                System.out.println("--- GEMINI ANALİZİ BAŞARILI ---");
                String jsonMetni = cevap.body();

                JsonObject outerResponse = JsonParser.parseString(jsonMetni).getAsJsonObject();
                JsonArray candidates = outerResponse.getAsJsonArray("candidates");

                if (candidates != null && candidates.size() > 0) {
                    String geminiText = candidates.get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();

                    String safJson = geminiText.replace("```json", "").replace("```", "").trim();
                    System.out.println("Ayıklanan Saf JSON: \n" + safJson);

                    Gson gson = new Gson();
                    Type listeTipi = new TypeToken<ArrayList<ZekaCiktisi>>() {}.getType();
                    sonuclar = gson.fromJson(safJson, listeTipi);
                    System.out.println("Gson Dönüşümü Başarılı! Yapay Zeka " + sonuclar.size() + " adet viral kesit tespit etti.");
                }
            }

        } catch (Exception e) {
            System.out.println("Yapay Zeka Hatası: " + e.getMessage());
        }
        return sonuclar;
    }
}