package islem;

import model.ZamanAraligi;
import java.util.List;

public interface AnalizMotoru {

    // Bu arayüzü kullanan her sınıf, videoyu inceleyip bize puanlanmış zaman aralıkları vermek ZORUNDADIR.
    List<ZamanAraligi> analizEt(String videoDosyaYolu);
}
