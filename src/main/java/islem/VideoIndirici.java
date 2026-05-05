package islem;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class VideoIndirici {

    private static final String KLASOR_INDIRILEN = "indirilen_videolar/";
    private static final String YTDLP_YOLU = "araclar/yt-dlp.exe";
    private static final String FFMPEG_YOLU = "araclar/ffmpeg.exe";

    public VideoIndirici() {
        File klasor = new File(KLASOR_INDIRILEN);
        if (!klasor.exists()) {
            klasor.mkdirs();
        }
    }

    public String videoIndir(String youtubeLink, String projeAdi) {
        System.out.println("İndirme işlemi başlatılıyor (SRT Formatlı)... Lütfen bekleyin.");

        try {
            ProcessBuilder komutGonderici = new ProcessBuilder(
                    YTDLP_YOLU,
                    "--write-auto-subs",
                    "--sub-langs", "tr",
                    "--convert-subs", "srt", // SRT'ye çevirme komutu
                    "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/mp4",
                    "-o", KLASOR_INDIRILEN + projeAdi + ".%(ext)s",
                    youtubeLink
            );

            komutGonderici.redirectErrorStream(true);
            Process islem = komutGonderici.start();

            BufferedReader okuyucu = new BufferedReader(new InputStreamReader(islem.getInputStream()));
            String satir;
            while ((satir = okuyucu.readLine()) != null) {
                // Logları gizledik, istersen sout(satir) ile açabilirsin.
            }

            int sonuc = islem.waitFor();

            if (sonuc == 0) {
                System.out.println("İndirme ve SRT dönüştürme BAŞARILI!");
                return KLASOR_INDIRILEN + projeAdi + ".mp4";
            } else {
                System.out.println("İndirme BAŞARISIZ! yt-dlp Hata Kodu: " + sonuc);
                return null;
            }

        } catch (Exception e) {
            System.out.println("HATA: " + e.getMessage());
            return null;
        }
    }

    public String seseDonustur(String videoYolu) {
        System.out.println("--- Analiz için ses ayıklanıyor... ---");
        String sesYolu = videoYolu.replace(".mp4", ".mp3");

        try {
            ProcessBuilder komutGonderici = new ProcessBuilder(
                    FFMPEG_YOLU,
                    "-y",
                    "-i", videoYolu,
                    "-q:a", "0",
                    "-map", "a",
                    sesYolu
            );

            komutGonderici.redirectErrorStream(true);
            Process islem = komutGonderici.start();
            islem.waitFor();

            return sesYolu;

        } catch (Exception e) {
            System.out.println("Ses ayıklama hatası: " + e.getMessage());
            return null;
        }
    }
}