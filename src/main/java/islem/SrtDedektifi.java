package islem;

import model.ZamanAraligi;
import java.util.List;
import java.util.Locale;

public class SrtDedektifi {

    public ZamanAraligi kelimelerdenSaniyeBul(String srtYolu, String ilkKelimeler, String sonKelimeler) {
        try {
            SrtOkuyucu okuyucu = new SrtOkuyucu();
            List<SrtOkuyucu.Kelime> tumKelimeler = okuyucu.tumKelimeleriGetir(srtYolu);

            if (tumKelimeler.isEmpty()) return null;

            String[] arananIlk = ucKelimeAl(ilkKelimeler, true);
            String[] arananSon = ucKelimeAl(sonKelimeler, false);

            double baslangicSaniyesi = -1;
            for (int i = 0; i <= tumKelimeler.size() - arananIlk.length; i++) {
                boolean match = true;
                for (int j = 0; j < arananIlk.length; j++) {
                    if (!tumKelimeler.get(i + j).temiz.equals(arananIlk[j])) { match = false; break; }
                }
                if (match) { baslangicSaniyesi = tumKelimeler.get(i).baslangic; break; }
            }

            double bitisSaniyesi = -1;
            for (int i = 0; i <= tumKelimeler.size() - arananSon.length; i++) {
                boolean match = true;
                for (int j = 0; j < arananSon.length; j++) {
                    if (!tumKelimeler.get(i + j).temiz.equals(arananSon[j])) { match = false; break; }
                }
                if (match) { bitisSaniyesi = tumKelimeler.get(i + arananSon.length - 1).bitis; break; }
            }

            if (baslangicSaniyesi != -1 && bitisSaniyesi != -1) {
                // Koruma kalkanı kaldırıldı, artık saf zamanlar dönüyor.
                return new ZamanAraligi(baslangicSaniyesi, bitisSaniyesi, 99);
            }

        } catch (Exception e) { System.out.println("SRT Dedektifi Hatası: " + e.getMessage()); }
        return null;
    }

    private String[] ucKelimeAl(String metin, boolean ilkMi) {
        String[] dizi = metin.replaceAll("<[^>]*>", "").replaceAll("[\\p{Punct}]", "").toLowerCase(new Locale("tr", "TR")).trim().split("\\s+");
        if (dizi.length >= 3) return ilkMi ? new String[]{dizi[0], dizi[1], dizi[2]} : new String[]{dizi[dizi.length - 3], dizi[dizi.length - 2], dizi[dizi.length - 1]};
        return dizi;
    }
}