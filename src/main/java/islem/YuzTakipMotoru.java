package islem;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;

public class YuzTakipMotoru {

    private CascadeClassifier yuzDedektoru;
    private static final String FFMPEG_YOLU = "araclar/ffmpeg.exe";
    private static final String KLASOR_KLIPLER = "uretilen_klipler/";

    public YuzTakipMotoru() {
        // OpenCV çekirdeğini işletim sistemine tanıtıyoruz (Ölümcül hataları engeller)
        OpenCV.loadLocally();

        // Senin az önce oluşturduğun 33 bin satırlık o beyin dosyasının yolunu gösteriyoruz
        String modelYolu = "araclar/haarcascade_frontalface_default.xml";
        this.yuzDedektoru = new CascadeClassifier(modelYolu);

        if (this.yuzDedektoru.empty()) {
            System.out.println("KRİTİK HATA: Yüz tespit modeli (haarcascade) yüklenemedi!");
        } else {
            System.out.println("Yüz Takip Motoru başarıyla devrede.");
        }
    }

    public int yuzunMerkeziniBul(String videoYolu, int klipBaslangici, int klipBitisi) {
        // Klibin tam ortasındaki saniyeyi buluyoruz (Örn: 10 ile 20 arasıysa, 15. saniye)
        int hedefSaniye = klipBaslangici + ((klipBitisi - klipBaslangici) / 2);

        String geciciGorselYolu = KLASOR_KLIPLER + "anlik_kare_" + hedefSaniye + ".jpg";

        // 1. AŞAMA: FFmpeg ile hedef saniyeden tek bir fotoğraf kopar
        boolean gorselAlindi = videodanKareKopar(videoYolu, hedefSaniye, geciciGorselYolu);

        // Varsayılan Merkez: 1920x1080 videonun tam ortası (Eğer yüz bulunamazsa burası döner)
        int merkezX = 960;

        if (gorselAlindi) {
            // 2. AŞAMA: OpenCV ile fotoğrafı analiz et
            Mat okunanGorsel = Imgcodecs.imread(geciciGorselYolu);
            MatOfRect tespitEdilenYuzler = new MatOfRect();

            // Yapay zeka fotoğraftaki yüzleri arıyor
            yuzDedektoru.detectMultiScale(okunanGorsel, tespitEdilenYuzler);

            Rect[] yuzDizisi = tespitEdilenYuzler.toArray();

            if (yuzDizisi.length > 0) {
                // Eğer yüz bulunduysa, ilk bulunan yüzün (genelde en büyük olanın) X merkezini hesapla
                // Formül: Yüzün başladığı sol nokta + (Yüzün Genişliği / 2)
                merkezX = yuzDizisi[0].x + (yuzDizisi[0].width / 2);
                System.out.println("KAMERA ODAKLANDI: Yüz " + merkezX + ". pikselde tespit edildi.");
            } else {
                System.out.println("UYARI: Bu karede net bir yüz bulunamadı, varsayılan merkeze dönülüyor.");
            }

            // Temizlik: Diski şişirmemek için geçici fotoğrafı siliyoruz
            new File(geciciGorselYolu).delete();
        }

        return merkezX;
    }

    private boolean videodanKareKopar(String videoYolu, int saniye, String ciktYolu) {
        try {
            ProcessBuilder komutGonderici = new ProcessBuilder(
                    FFMPEG_YOLU,
                    "-y",
                    "-ss", String.valueOf(saniye),
                    "-i", videoYolu,
                    "-vframes", "1", // Sadece 1 kare (frame) al
                    "-q:v", "2",     // Yüksek kalite
                    ciktYolu
            );

            komutGonderici.redirectErrorStream(true);
            Process islem = komutGonderici.start();
            islem.waitFor();

            return new File(ciktYolu).exists();

        } catch (Exception e) {
            System.out.println("Kare koparma hatası: " + e.getMessage());
            return false;
        }
    }
}