package model;

public class ZamanAraligi {
    private double baslangic;
    private double bitis;
    private int puan;

    public ZamanAraligi(double baslangic, double bitis, int puan) {
        this.baslangic = baslangic;
        this.bitis = bitis;
        this.puan = puan;
    }

    public double getBaslangicSaniyesi() { return baslangic; }
    public double getBitisSaniyesi() { return bitis; }
    public int getPuan() { return puan; }
}