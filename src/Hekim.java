
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import java.awt.Panel;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

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

    Database db;
    ResultSet result_bilgiler;
    ResultSet result_is_bilgileri;

    // bugunun tarihi
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    Date today = new Date();
    String str_today = formatter.format(today);

    private String ad_soyad;
    private String calisilan_firma;

    private Date is_baslangic_tarihi;
    private Date is_bitis_tarihi;

    private boolean calisiyormu = true;

    private int id;

//    private String[] hastalar = {};
    // aylik saglik taramasindan gecmeyen calisanlar
    private ArrayList<String> alist_aylik_taramadan_gecmeyen;

    String sorgu_hekim_bilgileri;
    String sorgu_is_bilgileri;

    public Hekim(Database db, int id) {

        this.id = id;
        this.db = db;
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
                    is_baslangic_tarihi = result_is_bilgileri.getDate("baslangic_tarihi");

                    is_bitis_tarihi = result_is_bilgileri.getDate("bitis_tarihi");
                    calisilan_firma = result_is_bilgileri.getString("Firma");
                }

                updateList(db);
            } else {
                calisilan_firma = "Aktif olarak çalıştığınız bir firma bulunmamaktadır!";
                is_baslangic_tarihi = null;
                is_bitis_tarihi = null;
                calisiyormu = false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Something happened durig setting informations: " + ex);
        }

    }

    public void updateList(Database db) {

        String sorgu_calisanlar;
        alist_aylik_taramadan_gecmeyen = new ArrayList<>();
        sorgu_calisanlar = "select Ad_soyad from calisan where id_calistigi_firma = '"
                + calisilan_firma + "' and  son_aylik_tarama <= NOW() - INTERVAL 1 MONTH";

        ResultSet result_calisanlar = db.query(sorgu_calisanlar);

        try {

            if (result_calisanlar.isBeforeFirst()) {
                while (result_calisanlar.next()) {
                    alist_aylik_taramadan_gecmeyen.add(result_calisanlar.getString("ad_soyad"));
                }

            } else {
                alist_aylik_taramadan_gecmeyen = null;
            }

        } catch (Exception e) {
            System.err.println("Some error during calisanlar: " + e);
        }
    }

    public boolean calisiyormu() {
        return calisiyormu;
    }

    public void muayeneEt() {

    }

    public boolean aylikSaglikTaramasi() {

        return false;
    }

    public void iseYeniGirisTaramasi() {

    }

    public boolean SaglikRiskiRaporu() throws SQLException {

        Document document = new Document();

        String rapor_dosya_ismi = "aylik_saglik_riski_" + calisilan_firma + "_" + str_today + ".pdf";
        String rapor_basligi = "AYLIK SAGLIK RISKI ANALIZ RAPORU";
        String sirket1 = "Firma:  ";
        String sirket2 = calisilan_firma.toUpperCase();

        int toplam_calisan_sayi = 0;
        int aylik_taramadan_gecmeyen = 0;

        if (!alist_aylik_taramadan_gecmeyen.isEmpty()) {
            aylik_taramadan_gecmeyen = alist_aylik_taramadan_gecmeyen.size();
        }

        int yeni_ise_giris_yapilmayan_sayi = 0;

        String calisan_sayi_sorgu = "Select Count(*) from calisan where id_calistigi_firma = '" + calisilan_firma + "'";
        String yeni_ise_giris_yapilmadi_sorgu = "Select Count(*) from calisan where yeni_giris_muayenesi = " + 0
                + " and id_calistigi_firma = '" + calisilan_firma + "'";

        ResultSet result_calisan_sayi = db.query(calisan_sayi_sorgu);
        ResultSet result_yeni_ise_giris = db.query(yeni_ise_giris_yapilmadi_sorgu);

        try {

            while (result_calisan_sayi.next()) {
                toplam_calisan_sayi = result_calisan_sayi.getInt("Count(*)");
            }

            while (result_yeni_ise_giris.next()) {

                yeni_ise_giris_yapilmayan_sayi = result_yeni_ise_giris.getInt("Count(*)");
            }

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(rapor_dosya_ismi));
            document.open();
//            document.add(new Paragraph("A Hello World PDF document."));

// turkce karakterler desteklenmiyor
            Font font_header = FontFactory.getFont(FontFactory.COURIER, 18, Font.BOLD, new CMYKColor(0, 255, 255, 0));
            Font font_sirket = FontFactory.getFont(FontFactory.COURIER, 14, new BaseColor(0, 0, 0));
            Font font_sirket2 = FontFactory.getFont(FontFactory.COURIER, 16, Font.BOLD, new BaseColor(0, 0, 0));
            Font font_tarih = FontFactory.getFont(FontFactory.COURIER, 14, new BaseColor(0, 0, 0));

            Paragraph header = new Paragraph(rapor_basligi, font_header);
            header.setAlignment(Element.ALIGN_CENTER);

            // rapor basligi ekle
            document.add(header);

            // bosluk ekle
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // firma ismini ekle
            Paragraph sirket = new Paragraph(sirket1, font_sirket);
            sirket.add(new Chunk(sirket2, font_sirket2));

            Chunk glue = new Chunk(new VerticalPositionMark());
            sirket.add(new Chunk(glue));
            Chunk tarih = new Chunk(str_today, font_tarih);
            sirket.add(tarih);
            document.add(sirket);

            // bosluk ekle
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            Font font_section_header = FontFactory.getFont(FontFactory.COURIER, 14, Font.BOLD, new BaseColor(0, 0, 0));

            // section 1 - bu ay muayeneden gecenler
            document.add(new Paragraph("Son bir ayda muayene edilen calisanlar:", font_section_header));
            String sorgu_bu_ay_muayene_olan = "SELECT c.ad_soyad, m.calisan_id, m.teshis, m.tarih FROM calisan c, muayene m where m.calisan_firma = '" + calisilan_firma + "' and tarih >= NOW() - INTERVAL 1 MONTH  and c.id = m.calisan_id";

            ResultSet result_muayene_olan = db.query(sorgu_bu_ay_muayene_olan);

            // tablo 1 olustur    
            PdfPTable table_bu_ay_muayene = new PdfPTable(4); // 4 columns.
            table_bu_ay_muayene.setWidthPercentage(100); //Width 100%
            table_bu_ay_muayene.setSpacingBefore(10f); //Space before table
            table_bu_ay_muayene.setSpacingAfter(10f); //Space after table

            //Set Column widths
            float[] columnWidths_muayene = {1f, 3f, 3f, 2f};
            table_bu_ay_muayene.setWidths(columnWidths_muayene);

            // tablo basliklarini olusturma
            Font font_table_header = FontFactory.getFont(FontFactory.COURIER, 14, Font.BOLD);

            PdfPCell cell_0 = new PdfPCell(new Paragraph("ID", font_table_header));
            cell_0.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell_0.setPaddingLeft(10);
            cell_0.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell_01 = new PdfPCell(new Paragraph("Ad Soyad", font_table_header));
            cell_01.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell_01.setPaddingLeft(10);
            cell_01.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell_02 = new PdfPCell(new Paragraph("Teshis", font_table_header));
            cell_02.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell_02.setPaddingLeft(10);
            cell_02.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell_03 = new PdfPCell(new Paragraph("Muayene Tarihi", font_table_header));
            cell_03.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell_03.setPaddingLeft(10);
            cell_03.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table_bu_ay_muayene.addCell(cell_0);
            table_bu_ay_muayene.addCell(cell_01);
            table_bu_ay_muayene.addCell(cell_02);
            table_bu_ay_muayene.addCell(cell_03);

            // veri tabanindan elde edilen degerler ile tablonun doldurulmasi
            if (result_muayene_olan.isBeforeFirst()) {
                while (result_muayene_olan.next()) {

                    int calisan_id = result_muayene_olan.getInt("m.calisan_id");
                    String muayene_ad_soyad = result_muayene_olan.getString("c.ad_soyad");
                    String muayene_teshis = result_muayene_olan.getString("m.teshis");
                    String muayene_tarihi = result_muayene_olan.getDate("m.tarih").toString();

                    PdfPCell cell_10 = new PdfPCell(new Paragraph(String.valueOf(calisan_id)));
                    cell_10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell_10.setPaddingLeft(10);
                    cell_10.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    PdfPCell cell_11 = new PdfPCell(new Paragraph(muayene_ad_soyad));
                    cell_11.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell_11.setPaddingLeft(10);
                    cell_11.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    PdfPCell cell_12 = new PdfPCell(new Paragraph(muayene_teshis));
                    cell_12.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell_12.setPaddingLeft(10);
                    cell_12.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    PdfPCell cell_13 = new PdfPCell(new Paragraph(muayene_tarihi));
                    cell_13.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell_13.setPaddingLeft(10);
                    cell_13.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    table_bu_ay_muayene.addCell(cell_10);
                    table_bu_ay_muayene.addCell(cell_11);
                    table_bu_ay_muayene.addCell(cell_12);
                    table_bu_ay_muayene.addCell(cell_13);
                }
            }

            // muayene tablosunun eklenmesi
            document.add(table_bu_ay_muayene);

            document.add(Chunk.NEWLINE);
            // section 1 - bu ay muayeneden gecenler
            document.add(new Paragraph("Aylik tarama ve ise giris tarama bilgileri:", font_section_header));
            // tablo olustur    
            PdfPTable table = new PdfPTable(2); // 2 columns.
            table.setWidthPercentage(100); //Width 100%
            table.setSpacingBefore(10f); //Space before table
            table.setSpacingAfter(10f); //Space after table

            //Set Column widths
            float[] columnWidths = {3f, 1f};
            table.setWidths(columnWidths);

            PdfPCell cell_1 = new PdfPCell(new Paragraph("Aylik saglik taramasindan gecen calisan sayi: "));
            cell_1.setPaddingLeft(10);
            cell_1.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell_11 = new PdfPCell(new Paragraph(String.valueOf(toplam_calisan_sayi - aylik_taramadan_gecmeyen)));
            cell_11.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell_11.setPaddingLeft(10);
            cell_11.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cell_1);
            table.addCell(cell_11);

            PdfPCell cell_2 = new PdfPCell(new Paragraph("Aylik saglik taramasindan gecmeyen calisan sayi: "));
            cell_2.setPaddingLeft(10);
            cell_2.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell_21 = new PdfPCell(new Paragraph(String.valueOf(aylik_taramadan_gecmeyen)));
            cell_21.setPaddingLeft(10);
            cell_21.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell_21.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cell_2);
            table.addCell(cell_21);

            PdfPCell cell_3 = new PdfPCell(new Paragraph("Yeni ise giris taramasindan gecen calisan sayi: "));
            cell_3.setPaddingLeft(10);
            cell_3.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell_31 = new PdfPCell(new Paragraph(String.valueOf(toplam_calisan_sayi - yeni_ise_giris_yapilmayan_sayi)));
            cell_31.setPaddingLeft(10);
            cell_31.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell_31.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cell_3);
            table.addCell(cell_31);

            PdfPCell cell_4 = new PdfPCell(new Paragraph("Yeni ise giris taramasindan gecmeyen calisan sayi: "));
            cell_4.setPaddingLeft(10);
            cell_4.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell_41 = new PdfPCell(new Paragraph(String.valueOf(yeni_ise_giris_yapilmayan_sayi)));
            cell_41.setPaddingLeft(10);
            cell_41.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell_41.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cell_4);
            table.addCell(cell_41);

            Font font_last_row = FontFactory.getFont(FontFactory.TIMES, 14, Font.BOLD, new BaseColor(0, 0, 0));

            PdfPCell cell_5 = new PdfPCell(new Paragraph("Toplam calisan sayi: "));
            cell_5.setPaddingLeft(10);
            cell_5.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell_51 = new PdfPCell(new Paragraph(String.valueOf(toplam_calisan_sayi), font_last_row));
            cell_51.setPaddingLeft(10);
            cell_51.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell_51.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cell_5);
            table.addCell(cell_51);

            document.add(table);


            /* ~~~~~~~~~~~~~~~  Tablo bitti ~~~~~~~~~~~~~~~~~~~~~*/
            // bosluk ekle
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            Paragraph hekim_ad_soyad = new Paragraph(this.ad_soyad);
            hekim_ad_soyad.setAlignment(Element.ALIGN_RIGHT);

            document.add(hekim_ad_soyad);
            document.close();
            writer.close();
            return true;

        } catch (DocumentException | FileNotFoundException e) {

            System.err.println("Some error during creating report: " + e);
            return false;
        }
    }

// GETTER AND SETTER
    public String getAd_soyad() {
        return ad_soyad;
    }

    public void setAd_soyad(String ad_soyad) {
        this.ad_soyad = ad_soyad;
    }

    public String getIs_baslangic_tarihi() {

        if (is_baslangic_tarihi != null) {
            return is_baslangic_tarihi.toString();
        } else {
            return "-";
        }
    }

    public void setIs_baslangic_tarihi(Date is_baslangic_tarihi) {
        this.is_baslangic_tarihi = is_baslangic_tarihi;
    }

    public String getIs_bitis_tarihi() {
        if (is_bitis_tarihi != null) {
            return is_bitis_tarihi.toString();
        } else {
            return "-";
        }
    }

    public void setIs_bitis_tarihi(Date is_bitis_tarihi) {
        this.is_bitis_tarihi = is_bitis_tarihi;
    }

    public String getCalisilan_firma() {
        return calisilan_firma;
    }

    public void setCalisilan_firma(String calisilan_firma) {
        this.calisilan_firma = calisilan_firma;
    }

    public ArrayList<String> getAylik_calisanlar() {
        return alist_aylik_taramadan_gecmeyen;
    }

    public void setAylik_calisanlar(ArrayList<String> aylik_calisanlar) {
        this.alist_aylik_taramadan_gecmeyen = aylik_calisanlar;
    }

}
