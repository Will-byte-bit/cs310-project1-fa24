package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        String result  = "";
        try {
           
            
            //remvoe later
            CSVReader csvReader = new CSVReader(new StringReader(getCsvString(csv)));
            CSVReader reader = new CSVReaderBuilder(new StringReader(getCsvString(csv))).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();

            List<String[]> full = csvReader.readAll();
            //end remove
            
            Iterator<String[]> iterator = csv.iterator();
            
            
            
            JsonObject main = new JsonObject();
            JsonObject scheduleType = new JsonObject();
            JsonObject subject = new JsonObject();
            JsonObject course = new JsonObject();
            
            
            JsonArray section = new JsonArray();
            JsonArray colheaders = new JsonArray();
            
            
            //gets headers
            for (String string : iterator.next()){
                colheaders.add(string);
  
            }
          
            while(iterator.hasNext()){
                
                String[] rows = iterator.next();
               
                 //testing if num and only abrev, IE acc, is held within subject.
                String numShort = rows[2].substring(0, rows[2].length()-4);
                String subjectId = numShort;
                String num = rows[2].substring(numShort.length()+1);
               
                int credits = Integer.parseInt(rows[6]);
                
              
                if(!scheduleType.containsKey(rows[5])){
                    scheduleType.put(rows[5], rows[11]);
                }
               
                if(!subject.containsKey(numShort)){
                    subject.put(numShort, rows[1]);
                }
                
                if(!course.containsKey(rows[2])){
                   
                    JsonObject subCourse = new JsonObject();
      
                    
                    //possibly change to use colheaders
                    subCourse.put("subjectid", subjectId);
                    subCourse.put("num", num);
                    subCourse.put("description", rows[3]);
                    subCourse.put("credits", credits);
                    
                            
                    
                   course.put(rows[2], subCourse);
                }
                JsonObject subSection = new JsonObject();
                subSection.put("crn", Integer.parseInt(rows[0]));
                subSection.put("subjectid", subjectId);
                subSection.put("num", num);
                subSection.put("section", rows[4]);
                subSection.put("type", rows[5]);
                subSection.put("start", rows[7]);
                subSection.put("end", rows[8]);
                subSection.put("days", rows[9]);
                subSection.put("where", rows[10]);
                
                JsonArray teachers = new JsonArray();
                
                //creates list of teachers from row data
                CSVReader reader2 = new CSVReaderBuilder(new StringReader(rows[12])).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).build();
                List<String[]> csv2 = reader2.readAll();
                Iterator<String[]> iterator2 = csv2.iterator();
                
                //runs through and adds to list
                for(String teacher : iterator2.next()){
                  
                    teachers.add(teacher.trim());
                }
               
                //nest inside subsection
                subSection.put("instructor", teachers);
                section.add(subSection);
    
                
            }
            
          
    
            main.put("scheduletype", scheduleType);
            main.put("subject", subject);
            main.put("course", course);
            main.put("section", section);
            
           
            result = Jsoner.serialize(main);
            result.trim();
            System.out.println(result);
            
        } catch (IOException ex) {
            Logger.getLogger(ClassSchedule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CsvException ex) {
            Logger.getLogger(ClassSchedule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result; // remove this!
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        String result = "";
        
        JsonArray scheduleType = (JsonArray)json.get("scheduletype");
        JsonArray subject = (JsonArray)json.get("subject");
        JsonArray course = (JsonArray)json.get("course");
        JsonArray section = (JsonArray)json.get("section");
        System.out.println(section);
        
        return ""; // remove this!
        
    }
    
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}