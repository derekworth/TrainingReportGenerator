package trainingreportgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
                try (BufferedReader br = new BufferedReader(fr)) {
                    while((line = br.readLine()) != null) {
                        if(count++ > 0) { // skip first line (headers)
                            String[] fields = line.split(",");
                            if(fields.length != 11) break;
                            String last, first, mi, ssn, rank, from, thru, awards, top3, gpa, csize;
                            last   = fields[0].toUpperCase();
                            first  = fields[1].toUpperCase();
                            mi     = fields[2].toUpperCase();
                            ssn    = fields[3];
                            awards = fields[4];
                            top3   = fields[5];
                            rank   = fields[6];
                            from   = fields[7];
                            thru   = fields[8];
                            gpa    = fields[9];
                            csize  = fields[10];
                            
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
                                String awardsBullet = "";
                                ((PDCheckBox) form.getField("DG")).check();
                                switch(awards) {
                                    case "AFCEA":
                                        ((PDCheckBox) form.getField("DG")).unCheck();
                                        criteria = "";
                                        awardsBullet = "\n- AFCEA Award winner (Top Contributor)";
                                        break;
                                    case "DG":
                                        awardsBullet = "\n- Distinguished Graduate";
                                        break;
                                    case "DG_AFCEA":
                                        awardsBullet = "\n- AFCEA Award winner (Top Contributor) and Distinguished Graduate";
                                        break;
                                    case "DG_Sadler":
                                        awardsBullet = "\n- Sadler Award winner (Highest Academic Average) and Distinguished Graduate";
                                        break;
                                    case "DG_Sadler_AFCEA":
                                        awardsBullet = "\n- Sadler Award winner (Highest Academic Average), AFCEA Award winner (Top Contributor), and DG";
                                        break;
                                    case "Sadler":
                                        ((PDCheckBox) form.getField("DG")).unCheck();
                                        criteria = "";
                                        awardsBullet = "\n- Sadler Award winner (Highest Academic Average)";
                                        break;
                                    case "Sadler_AFCEA":
                                        ((PDCheckBox) form.getField("DG")).unCheck();
                                        criteria = "";
                                        awardsBullet = "\n- Sadler (Highest Academic Average) and AFCEA (Top Contributor) Awards winner";
                                        break;
                                    case "TG":
                                        awardsBullet = "\n- Top Graduate";
                                        break;
                                    case "TG_AFCEA":
                                        awardsBullet = "\n- AFCEA Award winner (Top Contributor) and Top Graduate";
                                        break;
                                    case "TG_Sadler":
                                        awardsBullet = "\n- Sadler Award winner (Highest Academic Average) and Top Graduate";
                                        break;
                                    case "TG_Sadler_AFCEA":
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
                }
            }
        } catch(IOException e) { /*DO NOTHING*/ }
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
    
}
