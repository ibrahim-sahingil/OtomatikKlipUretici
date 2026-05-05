package islem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SrtOkuyucu {

    public static class Kelime {
        public String metin;
        public String temiz;
        public double baslangic;
        public double bitis;

        public Kelime(String metin, String temiz, double baslangic, double bitis) {
            this.metin = metin; this.temiz = temiz; this.baslangic = baslangic; this.bitis = bitis;
        }
    }

    private static class SrtBlok {
        double baslangic; String metin;
        public SrtBlok(double b, String m) { baslangic=b; metin=m; }
    }

    public List<Kelime> tumKelimeleriGetir(String srtYolu) {
        List<Kelime> tumKelimeler = new ArrayList<>();
        List<SrtBlok> hamBloklar = new ArrayList<>();

        try {
            List<String> satirlar = Files.readAllLines(Path.of(srtYolu));
            double anlikBaslangic = -1; StringBuilder anlikMetin = new StringBuilder();

            for (String satir : satirlar) {
                satir = satir.trim();
                if (satir.isEmpty()) {
                    if (anlikBaslangic != -1 && anlikMetin.length() > 0) hamBloklar.add(new SrtBlok(anlikBaslangic, anlikMetin.toString().trim()));
                    anlikBaslangic = -1; anlikMetin.setLength(0);
                } else if (satir.contains("-->")) {
                    anlikBaslangic = saniyeCevir(satir.split("-->")[0].trim());
                } else if (!Character.isDigit(satir.charAt(0)) || satir.length() > 5) {
                    satir = satir.replaceAll("<[^>]*>", "");
                    if (!satir.trim().isEmpty()) anlikMetin.append(satir).append(" ");
                }
            }
            if (anlikBaslangic != -1 && anlikMetin.length() > 0) hamBloklar.add(new SrtBlok(anlikBaslangic, anlikMetin.toString().trim()));

            List<String> oncekiTemiz = new ArrayList<>();
            for (SrtBlok blok : hamBloklar) {
                String[] orijinal = blok.metin.trim().split("\\s+");
                if (orijinal.length == 0 || orijinal[0].isEmpty()) continue;

                List<String> simdikiTemiz = new ArrayList<>();
                for (String w : orijinal) simdikiTemiz.add(harfleriTemizle(w));

                int maxOverlap = 0;
                for (int i = 0; i < oncekiTemiz.size(); i++) {
                    int o = 0;
                    while (i + o < oncekiTemiz.size() && o < simdikiTemiz.size() && oncekiTemiz.get(i + o).equals(simdikiTemiz.get(o))) o++;
                    if (i + o == oncekiTemiz.size() && o > maxOverlap) maxOverlap = o;
                }

                for (int i = maxOverlap; i < orijinal.length; i++) {
                    tumKelimeler.add(new Kelime(orijinal[i], simdikiTemiz.get(i), blok.baslangic, 0));
                }
                oncekiTemiz = simdikiTemiz;
            }

            // --- YENİ EKLENEN ÇÖZÜM 1: ZAMAN DAĞITIMI (INTERPOLATION) ---
            // Aynı saniyeye yığılan (alt alta çıkan) kelimeleri tespit edip zamanı onlara eşit paylaştırır.
            for (int i = 0; i < tumKelimeler.size(); i++) {
                int ayniZamanliKelimeSayisi = 1;
                double gecerliBaslangic = tumKelimeler.get(i).baslangic;

                while (i + ayniZamanliKelimeSayisi < tumKelimeler.size() &&
                        tumKelimeler.get(i + ayniZamanliKelimeSayisi).baslangic == gecerliBaslangic) {
                    ayniZamanliKelimeSayisi++;
                }

                if (ayniZamanliKelimeSayisi > 1) {
                    double sonrakiZaman = gecerliBaslangic + 2.0; // Varsayılan boşluk
                    if (i + ayniZamanliKelimeSayisi < tumKelimeler.size()) {
                        sonrakiZaman = tumKelimeler.get(i + ayniZamanliKelimeSayisi).baslangic;
                    }

                    double kelimeBasinaDusenSure = (sonrakiZaman - gecerliBaslangic) / ayniZamanliKelimeSayisi;
                    for (int j = 0; j < ayniZamanliKelimeSayisi; j++) {
                        tumKelimeler.get(i + j).baslangic = gecerliBaslangic + (j * kelimeBasinaDusenSure);
                    }
                }
                i += ayniZamanliKelimeSayisi - 1;
            }

            // --- YENİ EKLENEN ÇÖZÜM 2: SESSİZLİK UZAMASINI ENGELLEME ---
            // SrtOkuyucu.java içindeki tumKelimeleriGetir metodunun sonundaki for döngüsü:

            // --- YENİ EKLENEN ÇÖZÜM 2: SESSİZLİK UZAMASINI ENGELLEME ---
            for (int i = 0; i < tumKelimeler.size() - 1; i++) {
                double fark = tumKelimeler.get(i + 1).baslangic - tumKelimeler.get(i).baslangic;
                // Kelimeler çok uzun ekranda kalmasın
                tumKelimeler.get(i).bitis = tumKelimeler.get(i).baslangic + Math.min(fark - 0.02, 0.4);
            }

            // 2. KALİBRASYON: Son kelimenin videoyu uzatmasını engellemek için +0.5 yerine +0.2 yapıyoruz.
            if (!tumKelimeler.isEmpty()) tumKelimeler.get(tumKelimeler.size() - 1).bitis = tumKelimeler.get(tumKelimeler.size() - 1).baslangic + 0.2;

        } catch (Exception e) { System.out.println("HATA: SRT Okunamadı."); }
        return tumKelimeler;
    }

    public List<Kelime> klibeAitAltyazilariGetir(String srtYolu, double klipBaslangic, double klipBitis) {
        List<Kelime> klipKelimeleri = new ArrayList<>();
        double aramaBaslangic = Math.max(0, klipBaslangic - 0.2);
        double aramaBitis = klipBitis + 0.2;

        for (Kelime k : tumKelimeleriGetir(srtYolu)) {
            if (k.baslangic >= aramaBaslangic && k.baslangic <= aramaBitis) {
                klipKelimeleri.add(k);
            }
        }
        return klipKelimeleri;
    }

    private String harfleriTemizle(String metin) {
        return metin.replaceAll("[\\p{Punct}]", "").toLowerCase(new Locale("tr", "TR")).trim();
    }

    private double saniyeCevir(String zaman) {
        try {
            String[] p = zaman.split(","); String[] hms = p[0].split(":");
            return (Integer.parseInt(hms[0]) * 3600) + (Integer.parseInt(hms[1]) * 60) + Integer.parseInt(hms[2]) + Double.parseDouble("0." + p[1]);
        } catch (Exception e) { return 0; }
    }
}