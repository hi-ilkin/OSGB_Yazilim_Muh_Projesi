
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ilkin
 */
public class Analist {

    Database db;
    private String ad_soyad;

    public Analist(Database db) {
        this.db = db;

    }

    public DefaultTableModel getModel() {
        String sorgu = "Select * from randevu where tarih >= NOW() order by tarih";
        ResultSet rs = db.query(sorgu);

        try {

            // veri tabaninda bulunan satir sayi
            int columncount = rs.getMetaData().getColumnCount();

            // tablo icin model olusturma
            DefaultTableModel model = new DefaultTableModel();

            //tabloya sutun isimleri eklenir
            model.addColumn("Firmanın ismi");
            model.addColumn("İleşim Numarası");
            model.addColumn("Firmanın Adresi");
            model.addColumn("Randevu tarihi");
            while (rs.next()) {

                Object[] row = {rs.getString("sirket"), rs.getInt("telefon"), rs.getString("adres"), rs.getDate("tarih")};
                model.addRow(row);
                
                System.out.println(row);
            }
            
            
            
            
            return model;
            
        } catch (SQLException ex) {
            Logger.getLogger(Analist.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String getAd_soyad() {
        return ad_soyad;
    }

    public void setAd_soyad(String ad_soyad) {
        this.ad_soyad = ad_soyad;
    }
}
