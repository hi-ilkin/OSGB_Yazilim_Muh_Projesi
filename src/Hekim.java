
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ilkin
 */
public class Hekim {

    Database db ; 
    ResultSet result_bilgiler;
    ResultSet result_is_bilgileri;

    // bugunun tarihi
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    Date today = new Date();
    String str_today = formatter.format(today);

    private String ad_soyad;
    private String is_baslangic_tarihi;
    private String is_bitis_tarihi;
    private String calisilan_firma;
    private int id;
    
    String sorgu_hekim_bilgileri;
    String sorgu_is_bilgileri;
    
    public Hekim(Database db, int id) {
             
        this.id = id;
        
        
        // sorgular 
    sorgu_hekim_bilgileri = "Select hekim_ad_soyad from hekimler where id = " + id;
    sorgu_is_bilgileri = "Select firma, baslangic_tarihi, bitis_tarihi from is_etkinlikleri "
            + "where hekim_id = " + id + " and ('" + str_today + "' BETWEEN Baslangic_tarihi and Bitis_tarihi)";

        
        
        result_bilgiler = db.query(sorgu_hekim_bilgileri);
        result_is_bilgileri = db.query(sorgu_is_bilgileri);

        try {
            while (result_bilgiler.next()) {
                ad_soyad = result_bilgiler.getString("hekim_ad_soyad");
            }

            // girilen tarihler arasinda hekimin calistigi bir firma var mi?
            if (result_is_bilgileri.isBeforeFirst()) {
               
                while (result_is_bilgileri.next()) {
                    is_baslangic_tarihi = result_is_bilgileri.getDate("baslangic_tarihi").toString();
                    is_bitis_tarihi = result_is_bilgileri.getDate("bitis_tarihi").toString();
                    calisilan_firma = result_is_bilgileri.getString("Firma");
                }
            }
            else{
                calisilan_firma = "Aktif olarak çalıştığınız bir firma bulunmamaktadır!";
                is_baslangic_tarihi = "-";
                is_bitis_tarihi = "-";
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Something happened durig setting informations: " + ex);
        }

    }

    public void muayeneEt() {

    }

    public boolean aylikSaglikTaramasi() {

        return false;
    }

    public void iseYeniGirisTaramasi() {

    }

    public String SaglikRiskiRaporu() {

        return "";
    }

// GETTER AND SETTER
    public String getAd_soyad() {
        return ad_soyad;
    }

    public void setAd_soyad(String ad_soyad) {
        this.ad_soyad = ad_soyad;
    }

    public String getIs_baslangic_tarihi() {
        return is_baslangic_tarihi;
    }

    public void setIs_baslangic_tarihi(String is_baslangic_tarihi) {
        this.is_baslangic_tarihi = is_baslangic_tarihi;
    }

    public String getIs_bitis_tarihi() {
        return is_bitis_tarihi;
    }

    public void setIs_bitis_tarihi(String is_bitis_tarihi) {
        this.is_bitis_tarihi = is_bitis_tarihi;
    }

    public String getCalisilan_firma() {
        return calisilan_firma;
    }

    public void setCalisilan_firma(String calisilan_firma) {
        this.calisilan_firma = calisilan_firma;
    }

}
