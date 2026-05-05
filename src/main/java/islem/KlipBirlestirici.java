package islem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;

public class KlipBirlestirici {

    private static final String FFMPEG_YOLU = "araclar/ffmpeg.exe";
    private static final String KLASOR_KLIPLER = "uretilen_klipler/";

    // Parametre olarak birleştirilecek videoların listesini ve final dosyasının adını alıyoruz
    public boolean klipleriBirlestir(List<String> birlestirilecekKlipler, String finalDosyaAdi) {

        String listeDosyasiYolu = KLASOR_KLIPLER + "birlestirme_listesi.txt";
        String hedefDosyaYolu = KLASOR_KLIPLER + finalDosyaAdi + ".mp4";

        try {
            // 1. ADIM: FFmpeg'in okuyacağı txt listesini oluşturma
            File listeDosyasi = new File(listeDosyasiYolu);
            FileWriter yazici = new FileWriter(listeDosyasi);

            for (String klipYolu : birlestirilecekKlipler) {
                // Not: FFmpeg dosya yollarını okurken ters slash (\) değil, düz slash (/) sever.
                // Bu yüzden yolları güvenli hale getiriyoruz.
                String guvenliYol = klipYolu.replace("\\", "/");

                // txt dosyasına "file 'dosya_yolu'" formatında yazdırıyoruz
                yazici.write("file '" + guvenliYol + "'\n");
            }
            yazici.close();
            System.out.println("Birleştirme listesi başarıyla oluşturuldu.");

            // 2. ADIM: FFmpeg'e birleştirme komutunu gönderme
            /*
             * CMD Karşılığı: ffmpeg -f concat -safe 0 -i liste.txt -c copy final_ozet.mp4
             * -c copy : Videoları yeniden renderlamadan saniyeler içinde doğrudan birbirine yapıştırır (Sıfır kalite kaybı).
             */
            ProcessBuilder komutGonderici = new ProcessBuilder(
                    FFMPEG_YOLU,
                    "-y", // Eğer aynı isimde final dosyası varsa üzerine yaz
                    "-f", "concat",
                    "-safe", "0",
                    "-i", listeDosyasiYolu,
                    "-c", "copy",
                    hedefDosyaYolu
            );

            System.out.println("Klipler birleştiriliyor... Lütfen bekleyin.");
            Process calisanIslem = komutGonderici.start();

            // FFmpeg çıktılarını (Hata Akışını) okuyoruz
            BufferedReader konsolOkuyucu = new BufferedReader(new InputStreamReader(calisanIslem.getErrorStream()));
            String satir;
            while ((satir = konsolOkuyucu.readLine()) != null) {
                System.out.println("FFmpeg Montaj: " + satir);
            }

            int cikisKodu = calisanIslem.waitFor();

            // İşlem bittikten sonra arkada çöp bırakmamak için txt listesini siliyoruz
            listeDosyasi.delete();

            if (cikisKodu == 0) {
                System.out.println("MONTAJ BAŞARILI! Final Video: " + hedefDosyaYolu);
                return true;
            } else {
                System.out.println("HATA: Montaj işlemi başarısız oldu. Çıkış Kodu: " + cikisKodu);
                return false;
            }

        } catch (Exception hata) {
            System.out.println("Montaj sırasında sistemsel hata: " + hata.getMessage());
            return false;
        }
    }
}
