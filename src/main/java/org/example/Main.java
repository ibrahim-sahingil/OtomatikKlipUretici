package org.example;

import islem.AnaYonetici;

public class Main {
    public static void main(String[] args) {

        System.out.println("=== OPUS CLIP OTOMASYONU BAŞLATILIYOR ===");

        // Bütün sistemi yönetecek olan Şef'i (AnaYonetici) işe alıyoruz
        AnaYonetici yonetici = new AnaYonetici();

        // Şef'e sadece YouTube linkini ve projenin adını veriyoruz.
        // Gerisini (İndirme, Gemini Analizi, Kesme, Altyazı, Birleştirme) o halledecek.
        String youtubeLink = "https://youtu.be/aaGw_PtkJDA?si=W34R1Dxd_u0v-UuF";
        String projeAdi = "evrim_agaci_test";

        // Motorları çalıştır!
        yonetici.otomatikKlipUret(youtubeLink, projeAdi);

    }
}