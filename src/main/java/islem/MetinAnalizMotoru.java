package islem;

import model.ZamanAraligi;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MetinAnalizMotoru implements AnalizMotoru{

    // Sistemimizin avlayacağı sihirli kelimeler. Burayı projenin amacına göre zenginleştirebilirsin.
    private String[] anahtarKelimeler = {
            "önemli", "kısacası", "özetle", "sonuç olarak",
            "dikkat ederseniz", "en iyi", "harika", "mükemmel", "aslında"
    };

    @Override
    public List<ZamanAraligi> analizEt(String videoDosyaYolu) {
        List<ZamanAraligi> onemliAnlar = new ArrayList<>();

        // Mantık: İnen videonun adı "video.mp4" ise, altyazısı "video.vtt" veya "video.tr.vtt" olur.
        // Şimdilik test için standart .vtt uzantısını arayacağız.
        String altyaziDosyaYolu = videoDosyaYolu.replace(".mp4", ".tr.vtt");

        try (BufferedReader okuyucu = new BufferedReader(new FileReader(altyaziDosyaYolu))) {
            String satir;
            int anlikBaslangicSn = 0;
            int anlikBitisSn = 0;

            System.out.println("Metin Analizi Başladı... Altyazı dosyası ışık hızında taranıyor.");

            while ((satir = okuyucu.readLine()) != null) {

                // 1. ADIM: Zaman Damgası (Timestamp) Satırını Yakala
                // Örnek: 00:01:20.000 --> 00:01:25.000
                if (satir.contains("-->")) {
                    String[] zamanParcalari = satir.split("-->");
                    anlikBaslangicSn = zamaniSaniyeyeCevir(zamanParcalari[0].trim());
                    anlikBitisSn = zamaniSaniyeyeCevir(zamanParcalari[1].trim());
                }
                // 2. ADIM: Konuşma Satırını Yakala ve Kelime Ara
                else if (!satir.trim().isEmpty() && anlikBaslangicSn > 0) {

                    String kucukHarfliSatir = satir.toLowerCase(); // Büyük/küçük harf duyarlılığını kaldır

                    for (String kelime : anahtarKelimeler) {
                        if (kucukHarfliSatir.contains(kelime)) {

                            // Sihirli kelimeyi bulduk! Kelimeler çok net bağlam taşıdığı için buna yüksek (95) puan veriyoruz.
                            // Klibin çok erken kesilmemesi için bitiş süresine +10 saniye tolerans ekliyoruz.
                            ZamanAraligi yeniKlip = new ZamanAraligi(anlikBaslangicSn, anlikBitisSn + 10, 95);
                            onemliAnlar.add(yeniKlip);

                            System.out.println("Vurgulu Cümle Yakalandı: '" + satir + "' (Saniye: " + anlikBaslangicSn + ")");
                            break; // Aynı satırda başka kelime aramaya gerek yok, döngüyü kır
                        }
                    }
                    // Satır incelendiğine göre sonraki cümleyi beklemek üzere zamanı sıfırla
                    anlikBaslangicSn = 0;
                }
            }
            System.out.println("Metin Analizi Tamamlandı! Kelimelere göre " + onemliAnlar.size() + " adet nokta atışı sahne bulundu.");

        } catch (Exception hata) {
            System.out.println("Uyarı: Altyazı dosyası bulunamadı veya okunamadı. (" + altyaziDosyaYolu + ")");
            System.out.println("Sebebi: yt-dlp ile video indirirken altyazıyı da indirmesini söylememiş olabiliriz.");
        }

        return onemliAnlar;
    }

    // "00:01:20.000" gibi metin formatındaki bir zamanı matematikteki saf saniyeye (80) çeviren yardımcı araç
    private int zamaniSaniyeyeCevir(String zamanMetni) {
        try {
            // Saat, Dakika ve Saniye olarak ayır
            String[] anaParcalar = zamanMetni.split(":");
            int saat = Integer.parseInt(anaParcalar[0]);
            int dakika = Integer.parseInt(anaParcalar[1]);

            // Saniye kısmı milisaniye (.000) içerebilir, ondan kurtulalım
            String saniyeKisimi = anaParcalar[2].split("\\.")[0];
            int saniye = Integer.parseInt(saniyeKisimi);

            return (saat * 3600) + (dakika * 60) + saniye;
        } catch (Exception hata) {
            return 0; // Hata olursa 0. saniye kabul et
        }
    }
}
