package islem;

import model.Klip;
import model.ZamanAraligi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnaYonetici {

    private VideoIndirici indirici;
    private YapayZekaMotoru zekaMotoru;
    private KlipKesici kesici;
    private KlipBirlestirici birlestirici;
    private YuzTakipMotoru yuzMotoru;
    private SrtDedektifi srtDedektifi;

    public AnaYonetici() {
        this.indirici = new VideoIndirici();
        this.zekaMotoru = new YapayZekaMotoru();
        this.kesici = new KlipKesici();
        this.birlestirici = new KlipBirlestirici();
        this.yuzMotoru = new YuzTakipMotoru();
        this.srtDedektifi = new SrtDedektifi();
    }

    private boolean sistemBilesenleriniKontrolEt() {
        File ffmpeg = new File("araclar/ffmpeg.exe");
        File ytdlp = new File("araclar/yt-dlp.exe");

        if (!ffmpeg.exists() || !ytdlp.exists()) {
            System.out.println("ÖLÜMCÜL HATA: araclar klasöründe ffmpeg.exe veya yt-dlp.exe bulunamadı!");
            return false;
        }
        return true;
    }

    public void otomatikKlipUret(String youtubeLink, String projeAdi) {
        System.out.println("=== OPUS V5.0 (MİLİSANİYE MİMARİSİ) BAŞLATILDI ===");

        if (!sistemBilesenleriniKontrolEt()) return;

        String inenVideoYolu = indirici.videoIndir(youtubeLink, projeAdi);
        if (inenVideoYolu == null) return;

        String srtYolu = inenVideoYolu.replace(".mp4", ".tr.srt");

        System.out.println("\n--- Yapay Zeka Katmanı Analizi Başlıyor ---");
        List<YapayZekaMotoru.ZekaCiktisi> hamCiktilar = zekaMotoru.analizEt(srtYolu);

        if (hamCiktilar.isEmpty()) {
            System.out.println("MAALESEF: Yapay zeka geçerli bir sahne bulamadı.");
            return;
        }

        System.out.println("\n--- Tersine Mühendislik (Saniye Tespiti) Başlıyor ---");
        List<String> kesilenParcalarinYollari = new ArrayList<>();

        for (int i = 0; i < hamCiktilar.size(); i++) {
            YapayZekaMotoru.ZekaCiktisi cikti = hamCiktilar.get(i);

            ZamanAraligi an = srtDedektifi.kelimelerdenSaniyeBul(srtYolu, cikti.ilk_kelimeler, cikti.son_kelimeler);

            if (an == null) {
                System.out.println("HATA: Yapay zekanın seçtiği cümleler altyazıda bulunamadı. Klip atlanıyor.");
                continue;
            }

            int sureFarki = (int) (an.getBitisSaniyesi() - an.getBaslangicSaniyesi());
            if (sureFarki < 10 || sureFarki > 120) {
                System.out.println("GÜVENLİK İHLALİ: Süre mantıksız (" + sureFarki + " sn). Atlanıyor.");
                continue;
            }

            String parcaAdi = projeAdi + "_parca_" + (i + 1) + ".mp4";

            System.out.println("\nKlip " + (i + 1) + " (" + sureFarki + " sn) için en iyi kamera açısı aranıyor...");

            // Yüz motoru frame analizi yaptığı için int kalabilir, bu altyazı senkronunu bozmaz.
            int merkezX = yuzMotoru.yuzunMerkeziniBul(inenVideoYolu, (int)an.getBaslangicSaniyesi(), (int)an.getBitisSaniyesi());

            SrtOkuyucu okuyucu = new SrtOkuyucu();

            // KRİTİK DÜZELTME: (int) casting işlemleri tamamen silindi! Saf double gönderiliyor.
            List<SrtOkuyucu.Kelime> cimbizlananKelimeler = okuyucu.klibeAitAltyazilariGetir(
                    srtYolu, an.getBaslangicSaniyesi(), an.getBitisSaniyesi()
            );

            AltyaziUretici tasarimci = new AltyaziUretici();
            String assDosyaYolu = tasarimci.assDosyasiOlustur(parcaAdi, cimbizlananKelimeler, an.getBaslangicSaniyesi());

            Klip yeniKlip = new Klip(parcaAdi, an.getBaslangicSaniyesi(), an.getBitisSaniyesi());

            boolean basariliMi = kesici.klibiKes(inenVideoYolu, yeniKlip, assDosyaYolu, merkezX);

            if (basariliMi) kesilenParcalarinYollari.add(parcaAdi);
        }

        if (kesilenParcalarinYollari.size() > 1) {
            System.out.println("\n--- Final Montaj Katmanı ---");
            String finalDosyaAdi = projeAdi + "_FINAL_OZET";
            birlestirici.klipleriBirlestir(kesilenParcalarinYollari, finalDosyaAdi);
        }

        System.out.println("\n=== OPUS V5.0 BAŞARIYLA TAMAMLANDI ===");
    }
}