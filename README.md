# 🎬 AI Destekli Otomatik Klip Üretici (Opus Clip Klonu)

Uzun YouTube videolarını yapay zeka ile analiz edip, viral potansiyeli yüksek kısa klipler (Shorts/Reels) üreten Java tabanlı bir video otomasyon projesi. 

Sıfırdan, Nesne Yönelimli Programlama (OOP) prensiplerine uygun olarak geliştirilmiştir.

## 🚀 Projenin Öne Çıkan Yanları

* **Metin ve Bağlam Analizi:** Gemini 2.5 Flash API ve özel istem mühendisliği (Prompt Engineering) kullanılarak, videonun en dikkat çekici kısımları saniye tahmini yapılmadan doğrudan kelime bazlı olarak tespit edilir.
* **Hassas Video Kesimi:** FFmpeg entegrasyonu ve yeniden kodlama (re-encode) yöntemiyle videolar milisaniye hassasiyetinde kesilir. Önceki veya sonraki cümlelerden kaynaklanan ses sızıntıları engellenerek akıcı bir bütünlük sağlanır.
* **Dinamik Altyazı Senkronizasyonu:** YouTube'un kayan altyazı (rolling subtitle) formatını ayrıştırmak için geliştirilen "DIFF ve Zaman Dağıtıcı (Interpolation)" algoritmaları sayesinde, kelimeler ekrana tam zamanında ve üst üste binmeden yansıtılır.

## 🛠️ Kullanılan Teknolojiler

* **Dil:** Java 
* **Video/Medya İşleme:** FFmpeg, yt-dlp
* **Yapay Zeka:** Google Gemini API
* **Veri Ayrıştırma & Formatlama:** Gson (JSON Parse), .ass SubStation Alpha (Altyazı Render)

## ⚙️ Nasıl Çalıştırılır?

1. Projeyi bilgisayarınıza klonlayın.
2. Proje dizinine `araclar` isimli bir klasör oluşturup içerisine `ffmpeg.exe` ve `yt-dlp.exe` dosyalarını ekleyin.
3. API anahtarının koda gömülmemesi için sisteminize `GEMINI_API_KEY` adında bir ortam değişkeni (Environment Variable) ekleyin.
4. `Main.java` sınıfını çalıştırarak otomasyonu başlatın ve üretilen klipleri proje dizininden inceleyin.
