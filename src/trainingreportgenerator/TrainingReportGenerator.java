package trainingreportgenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;

/**
 *
 * @author derekworth
 */
public class TrainingReportGenerator {

    public static void main(String args[]) throws IOException {
        
        try {
            String line;
            try (FileReader fr = new FileReader("Student-TR-Info.csv")) {
                int count = 0;
                int afcea = 0, sadler = 0, dg = 0, tg = 0, t1 = 0, t2 = 0, t3 = 0;
                try (BufferedReader br = new BufferedReader(fr)) {
                    while((line = br.readLine()) != null) {
                        if(count++ > 0) { // skip first line (headers)
                            String[] fields = line.split(",");
                            if(fields.length != 11) break;
                            String last, first, mi, ssn, rank, from, thru, awards, top3, gpa, csize;
                            rank   = formatRank(fields[0]);
                            last   = fields[1].toUpperCase();
                            first  = fields[2].toUpperCase();
                            mi     = fields[3].toUpperCase();
                            ssn    = fields[4];
                            gpa    = fields[5];
                            awards = fields[6];
                            top3   = fields[7];
                            csize  = fields[8];
                            from   = formatDate(fields[9]);
                            thru   = formatDate(fields[10]);
                            
                            // format SSN
                            if(ssn.length() == 9) {
                                ssn = ssn.substring(0, 3) + "-" + ssn.substring(3, 5) + "-" + ssn.substring(5, 9);
                            }
                            
                            // Make copy of Template
                            File source = new File("Student-TR.pdf");
                            File dest;
                            if(mi.length() > 0) {
                                dest = new File("475-" + last + "_" + first + "_" + mi + ".pdf");
                            } else {
                                dest = new File("475-" + last + "_" + first + ".pdf");
                            }
                            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            
                            try (PDDocument doc = PDDocument.load(dest)) {
                                PDDocumentCatalog catalog = doc.getDocumentCatalog();
                                PDAcroForm form = catalog.getAcroForm();
                                
                                String name;
                                if(mi.length() > 0) {
                                    name = last + ", " + first + " " + mi + ".";
                                } else {
                                    name = last + ", " + first;
                                }
                                form.getField("Name").setValue(name);
                                form.getField("SSN").setValue(ssn);
                                form.getField("FROM").setValue(from);
                                form.getField("THRU").setValue(thru);
                                form.getField("Rank").setValue(rank);
                                String criteria = "Course E3OBR17D1 002A - minimum GPA of 95%; no failing written or performance test grades; no academic washbacks or mandatory SIA; no disciplinary problems or derogatory comments on file; no unexcused absences.";
                                String top3Bullet = "";
                                if(top3.length() > 0) top3Bullet = "\n- Graduated from the course with an academic grade point average of " + 
                                        gpa + " percent (#" + top3 + "/" + csize + " students)";
                                if(top3.equalsIgnoreCase("1")) {
                                    t1++;
                                } else if(top3.equalsIgnoreCase("2")) {
                                    t2++;
                                } else if(top3.equalsIgnoreCase("3")) {
                                    t3++;
                                }
                                String awardsBullet = "";
                                ((PDCheckBox) form.getField("DG")).check();
                                switch(awards) {
                                    case "AFCEA":
                                        afcea++;
                                        ((PDCheckBox) form.getField("DG")).unCheck();
                                        criteria = "";
                                        awardsBullet = "\n- AFCEA Award winner (Top Contributor)";
                                        break;
                                    case "DG":
                                        dg++;
                                        awardsBullet = "\n- Distinguished Graduate";
                                        break;
                                    case "DG_AFCEA":
                                        dg++;
                                        afcea++;
                                        awardsBullet = "\n- AFCEA Award winner (Top Contributor) and Distinguished Graduate";
                                        break;
                                    case "DG_Sadler":
                                        dg++;
                                        sadler++;
                                        awardsBullet = "\n- Sadler Award winner (Highest Academic Average) and Distinguished Graduate";
                                        break;
                                    case "DG_Sadler_AFCEA":
                                        dg++;
                                        sadler++;
                                        afcea++;
                                        awardsBullet = "\n- Sadler Award winner (Highest Academic Average), AFCEA Award winner (Top Contributor), and DG";
                                        break;
                                    case "Sadler":
                                        sadler++;
                                        ((PDCheckBox) form.getField("DG")).unCheck();
                                        criteria = "";
                                        awardsBullet = "\n- Sadler Award winner (Highest Academic Average)";
                                        break;
                                    case "Sadler_AFCEA":
                                        sadler++;
                                        afcea++;
                                        ((PDCheckBox) form.getField("DG")).unCheck();
                                        criteria = "";
                                        awardsBullet = "\n- Sadler (Highest Academic Average) and AFCEA (Top Contributor) Awards winner";
                                        break;
                                    case "TG":
                                        tg++;
                                        awardsBullet = "\n- Top Graduate";
                                        break;
                                    case "TG_AFCEA":
                                        tg++;
                                        afcea++;
                                        awardsBullet = "\n- AFCEA Award winner (Top Contributor) and Top Graduate";
                                        break;
                                    case "TG_Sadler":
                                        tg++;
                                        sadler++;
                                        awardsBullet = "\n- Sadler Award winner (Highest Academic Average) and Top Graduate";
                                        break;
                                    case "TG_Sadler_AFCEA":
                                        tg++;
                                        sadler++;
                                        afcea++;
                                        awardsBullet = "\n- Sadler Award winner (Highest Academic Average), AFCEA Award winner (Top Contributor), and TG";
                                        break;
                                    default:
                                        ((PDCheckBox) form.getField("DG")).unCheck();
                                        criteria = "";
                                }
                                form.getField("DGCriteria").setValue(criteria);
                                form.getField("AcademicAccomplishments").setValue(form.getField("AcademicAccomplishments").getValueAsString() + top3Bullet + awardsBullet);
                                form.getField("DateSigned").setValue(getDatestamp(0));
                                form.getField("OtherComments").setValue("");
                                
                                // Save changes
                                doc.save(dest);
                                
                            }
                        }
                    }
                    writeToFile(count-1 + "x training reports successfully generated, including:\n" + 
                            "\n " + dg     + "x DG(s)" + 
                            "\n " + tg     + "x TG(s)" + 
                            "\n " + afcea  + "x AFCEA(s)" + 
                            "\n " + sadler + "x Sadler(s)" + 
                            "\n " + t1     + "x Top1(s)" + 
                            "\n " + t2     + "x Top2(s)" + 
                            "\n " + t3     + "x Top3(s)");
                }
            }
        } catch(IOException e) { writeToFile(e.getMessage()); }
    }  
    
    /**
     * Provides the current timestamp (date only)
     * @param fromToday offset from today (e.g. -1 = yesterday, 0 = today,
     * 2 = day after tomorrow)
     * @return timestamp in format: DD MMM YYYY
     */
    public static String getDatestamp(int fromToday) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, fromToday);
        return new SimpleDateFormat("dd MMM yyyy").format(c.getTime());
    }
    
    /**
     * Provides the current timestamp
     * @return timestamp in format: YYYY-MM-DD hh:mm:ss
     */
    public static String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }
    
    public static void writeToFile(String msg) {
        try {
            FileWriter fw = new FileWriter("results.txt");
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write("--Results as of " + getTimestamp() + "--");
                bw.write("\n\n" + msg);
            }
        } catch(IOException ex) {}
    }
    
    public static String formatDate(String date) {
        String[] tmp = date.split("-");
        if(tmp.length == 3 
            && tmp[0].length() > 0 && tmp[0].length() < 3
            && tmp[1].length() == 3
            && tmp[2].length() == 2) {
            String day, mth, yr;
            day = tmp[0];
            mth = tmp[1];
            yr  = tmp[2];
            // pad day as needed
            if(day.length() == 1) {
                day = "0" + day;
            }
            return day + " " + mth + " 20" + yr;
        }
        return date;
    }
    
    public static String formatRank(String rank) {
        if(rank.equalsIgnoreCase("2lt") || rank.equalsIgnoreCase("2dlt") || rank.equalsIgnoreCase("2d lt") || rank.equalsIgnoreCase("2nd lt")) {
            rank = "2LT";
        } else if(rank.equalsIgnoreCase("1lt") || rank.equalsIgnoreCase("1stlt") || rank.equalsIgnoreCase("1st lt")) {
            rank = "1LT";
        } else if(rank.equalsIgnoreCase("capt") || rank.equalsIgnoreCase("captain")) {
            rank = "Capt";
        } else if(rank.equalsIgnoreCase("maj") || rank.equalsIgnoreCase("major")) {
            rank = "Maj";
        } else if(rank.equalsIgnoreCase("col") || rank.equalsIgnoreCase("colon")) {
            rank = "Col";
        } else if(rank.equalsIgnoreCase("lt col")) {
            rank = "Lt Col";
        } else if(rank.equalsIgnoreCase("brig gen")) {
            rank = "Brig Gen";
        } else if(rank.equalsIgnoreCase("maj gen")) {
            rank = "Maj Gen";
        } else if(rank.equalsIgnoreCase("lt gen")) {
            rank = "Lt Gen";
        } else if(rank.equalsIgnoreCase("gen")) {
            rank = "Gen";
        }
        return rank;
    }
    
}
