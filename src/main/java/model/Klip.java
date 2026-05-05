package model;

public class Klip {
    private String dosyaAdi;
    private double baslangic;
    private double bitis;

    public Klip(String dosyaAdi, double baslangic, double bitis) {
        this.dosyaAdi = dosyaAdi;
        this.baslangic = baslangic;
        this.bitis = bitis;
    }

    public String getDosyaAdi() { return dosyaAdi; }
    public double getBaslangicSaniyesi() { return baslangic; }
    public double getBitisSaniyesi() { return bitis; }
}