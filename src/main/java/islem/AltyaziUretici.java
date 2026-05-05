package islem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Locale;

public class AltyaziUretici {

    private static final String KLASOR_KLIPLER = "uretilen_klipler/";

    public AltyaziUretici() {
        File klasor = new File(KLASOR_KLIPLER);
        if (!klasor.exists()) klasor.mkdirs();
    }

    public String assDosyasiOlustur(String klipAdi, List<SrtOkuyucu.Kelime> kelimeler, double klipBaslangicSaniyesi) {
        String assDosyaAdi = KLASOR_KLIPLER + klipAdi.replace(".mp4", ".ass");

        try (BufferedWriter yazici = new BufferedWriter(new FileWriter(assDosyaAdi))) {
            yazici.write("[Script Info]\nScriptType: v4.00+\nPlayResX: 1080\nPlayResY: 1920\n\n");
            yazici.write("[V4+ Styles]\nFormat: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n");
            yazici.write("Style: TiktokStyle,Arial,110,&H0000FFFF,&H000000FF,&H00000000,&H80000000,-1,0,0,0,100,100,0,0,1,5,4,2,10,10,400,1\n\n");
            yazici.write("[Events]\nFormat: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n");

            // KlipKesici'deki 0.2 saniyelik payı buraya da ekledik
            double gercekVideoBaslangici = klipBaslangicSaniyesi + 0.2;

            for (int i = 0; i < kelimeler.size(); i++) {
                SrtOkuyucu.Kelime aktif = kelimeler.get(i);
                double bitis = aktif.bitis;

                if (i + 1 < kelimeler.size() && bitis > kelimeler.get(i + 1).baslangic) {
                    bitis = kelimeler.get(i + 1).baslangic;
                }

                double startR = aktif.baslangic - gercekVideoBaslangici;
                double endR = bitis - gercekVideoBaslangici;

                if (endR <= 0) continue;

                // 3. KALİBRASYON: İlk kelime videonun başlangıcından önce gelse bile, onu silme.
                // Videonun 0.0. saniyesine sabitle ki izleyici videoyu açar açmaz ilk kelimeyi görsün.
                if (startR < 0) startR = 0.0;

                if (endR - startR < 0.1) endR = startR + 0.1;

                yazici.write(String.format(Locale.US, "Dialogue: 0,%s,%s,TiktokStyle,,0,0,0,,%s\n",
                        assZamanCevir(startR), assZamanCevir(endR), aktif.metin));
            }
            return assDosyaAdi;
        } catch (Exception e) { return null; }
    }

    private String assZamanCevir(double saniye) {
        if (saniye < 0) saniye = 0;
        int saat = (int) (saniye / 3600); int dakika = (int) ((saniye % 3600) / 60); double kalanSaniye = saniye % 60;
        return String.format(Locale.US, "%d:%02d:%05.2f", saat, dakika, kalanSaniye);
    }
}