package islem;

import model.Klip;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class KlipKesici {

    private static final String FFMPEG_YOLU = "araclar/ffmpeg.exe";
    private static final String KLASOR_KLIPLER = "uretilen_klipler/";

    public boolean klibiKes(String kaynakVideoYolu, Klip kesilecekKlip, String assDosyaAdi, int yuzMerkeziX) {
        String hedefDosyaYolu = KLASOR_KLIPLER + kesilecekKlip.getDosyaAdi();

        // 1. KALİBRASYON: Önceki kelimenin sızıntısını kesmek için 0.2 sn ileri itiyoruz.
        // Sonraki kelimeyi duymamak için bitiş payını 0.0 yapıyoruz.
        double marginBaslangic = 0.2;
        double marginBitis = 0.0;

        double gercekBaslangic = kesilecekKlip.getBaslangicSaniyesi() + marginBaslangic;
        double gercekBitis = kesilecekKlip.getBitisSaniyesi() + marginBitis;
        double klipSuresi = gercekBitis - gercekBaslangic;

        System.out.println("Hassas Kesim Başlıyor -> " + kesilecekKlip.getDosyaAdi() +
                " | Başlangıç: " + gercekBaslangic + " | Süre: " + klipSuresi);

        try {
            String guvenliAltyaziYolu = assDosyaAdi.replace("\\", "/").replace(":", "\\:");
            int scaledWidth = 1080; int scaledHeight = 608; int overlayY = 656;

            String filtreZinciri =
                    "[0:v]scale=3413:1920,gblur=sigma=20,crop=1080:1920:1166:0[bg_dikey];" +
                            "[0:v]scale=" + scaledWidth + ":" + scaledHeight + ",setsar=1/1[main_scaled];" +
                            "[bg_dikey][main_scaled]overlay=0:" + overlayY + ",fps=30,format=yuv420p,setsar=1/1[video_hazir];" +
                            "[video_hazir]ass='" + guvenliAltyaziYolu + "'[outv];" +
                            "[0:a]aresample=44100,aformat=channel_layouts=stereo[outa]";

            String formatliBaslangic = String.format(Locale.US, "%.3f", gercekBaslangic);
            String formatliSure = String.format(Locale.US, "%.3f", klipSuresi);

            ProcessBuilder komutGonderici = new ProcessBuilder(
                    FFMPEG_YOLU, "-y",
                    "-ss", formatliBaslangic,
                    "-i", kaynakVideoYolu,
                    "-t", formatliSure,
                    "-filter_complex", filtreZinciri,
                    "-map", "[outv]",
                    "-map", "[outa]",
                    "-c:v", "libx264",
                    "-preset", "fast",
                    "-crf", "23",
                    "-c:a", "aac",
                    "-b:a", "192k",
                    "-async", "1",
                    hedefDosyaYolu
            );

            komutGonderici.redirectErrorStream(true);
            Process islem = komutGonderici.start();
            BufferedReader okuyucu = new BufferedReader(new InputStreamReader(islem.getInputStream()));
            while (okuyucu.readLine() != null) {}
            return islem.waitFor() == 0;

        } catch (Exception e) { return false; }
    }
}