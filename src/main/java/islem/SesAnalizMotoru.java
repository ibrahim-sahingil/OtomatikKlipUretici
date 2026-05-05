package islem;

import model.ZamanAraligi;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// "implements AnalizMotoru" diyerek bu sınıfın şablonumuza uymak zorunda olduğunu belirtiyoruz

public class SesAnalizMotoru implements AnalizMotoru{

    private static final String FFMPEG_YOLU = "araclar/ffmpeg.exe";

    @Override
    public List<ZamanAraligi> analizEt(String videoDosyaYolu) {

        // Bulduğumuz önemli (sesli) anları bu listede toplayacağız
        List<ZamanAraligi> onemliAnlarListesi = new ArrayList<>();

        /*
         * FFmpeg'e şu komutu veriyoruz:
         * "Bu videoyu izle ama kaydetme (-f null). Sadece bana sesin -30 desibelin altına düştüğü
         * ve bu sessizliğin en az 1 saniye (d=1) sürdüğü yerlerin saniyelerini raporla."
         */
        ProcessBuilder komutGonderici = new ProcessBuilder(
                FFMPEG_YOLU,
                "-i", videoDosyaYolu,
                "-vn", // İŞTE SİHİRLİ KELİME: Görüntüyü tamamen yoksay (Video No)
                "-af", "silencedetect=noise=-30dB:d=1",
                "-f", "null",
                "-"
        );

        try {
            System.out.println("Ses Analiz Motoru videoyu dinliyor... Bu işlem videonun uzunluğuna göre biraz sürebilir.");
            Process calisanIslem = komutGonderici.start();

            BufferedReader konsolOkuyucu = new BufferedReader(new InputStreamReader(calisanIslem.getErrorStream()));
            String satir;

            // Mantık: Video 0. saniyeden başlar. Sessizlik başlayana kadar geçen süre "sesli/önemli" kısımdır.
            double sonSesliKisimBaslangici = 0.0;

            while ((satir = konsolOkuyucu.readLine()) != null) {

                // Eğer konsolda "silence_start" (sessizlik başladı) yazısı görürsek:
                if (satir.contains("silence_start: ")) {
                    // Satırdan sessizliğin başladığı saniyeyi çekip alıyoruz
                    String[] parcalar = satir.split("silence_start: ");
                    double sessizlikBaslangici = Double.parseDouble(parcalar[1].trim());

                    // Demek ki 'sonSesliKisimBaslangici' ile bu 'sessizlikBaslangici' arası dolu dolu bir sahne!
                    int baslangic = (int) sonSesliKisimBaslangici;
                    int bitis = (int) sessizlikBaslangici;

                    // Klipler çok kısa olmasın (örneğin en az 5 saniyelik konuşma/ses varsa listeye ekle)
                    if ((bitis - baslangic) >= 5) {
                        // Sadece sese dayalı olduğu için şimdilik bu parçalara 70 puan veriyoruz
                        ZamanAraligi yeniKlip = new ZamanAraligi(baslangic, bitis, 70);
                        onemliAnlarListesi.add(yeniKlip);
                        System.out.println("Önemli an bulundu: " + baslangic + ". saniye ile " + bitis + ". saniye arası.");
                    }
                }

                // Eğer konsolda "silence_end" (sessizlik bitti, yani tekrar ses/konuşma başladı) yazısı görürsek:
                if (satir.contains("silence_end: ")) {
                    String[] parcalar = satir.split("silence_end: ");
                    // Sonraki kelimenin başlangıç noktasını kaydediyoruz (Örn: 14.5 sn)
                    // '|' karakterinden öncesini alarak süreyi temiz bir sayıya çeviriyoruz
                    String temizSure = parcalar[1].split("\\|")[0].trim();
                    sonSesliKisimBaslangici = Double.parseDouble(temizSure);
                }
            }

            calisanIslem.waitFor();
            System.out.println("Ses Analizi Tamamlandı! Toplam " + onemliAnlarListesi.size() + " adet önemli sahne bulundu.");

        } catch (Exception hata) {
            System.out.println("Ses analizi sırasında hata oluştu: " + hata.getMessage());
        }

        // Bulduğumuz tüm listeyi ana programa geri gönderiyoruz
        return onemliAnlarListesi;
    }
}
