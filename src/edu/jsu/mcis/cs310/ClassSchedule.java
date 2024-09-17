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
import java.util.Arrays;
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
          
            Iterator<String[]> iterator = csv.iterator();
            
            //Objects to store data
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
                
                //placing data in section
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
            //System.out.println(result);
            
        } catch (IOException ex) {
            Logger.getLogger(ClassSchedule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CsvException ex) {
            Logger.getLogger(ClassSchedule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result; // remove this!
    }
    
    public String convertJsonToCsvString(JsonObject json) {

        

        List<String[]> result = new ArrayList();
        
        //base headers
        String[] header = {
            CRN_COL_HEADER,SUBJECT_COL_HEADER,NUM_COL_HEADER, 
            DESCRIPTION_COL_HEADER, SECTION_COL_HEADER, TYPE_COL_HEADER, 
            CREDITS_COL_HEADER, START_COL_HEADER, END_COL_HEADER, DAYS_COL_HEADER,
            WHERE_COL_HEADER, SCHEDULE_COL_HEADER, INSTRUCTOR_COL_HEADER,

        };
        result.add(header);
        System.out.println(header);
        
      
        //all base level objects from json
        JsonObject scheduleType = (JsonObject)json.get("scheduletype");
        JsonObject subject = (JsonObject)json.get("subject");
        JsonObject course = (JsonObject)json.get("course");
        JsonArray section = (JsonArray)json.get("section");
      
        for(int i = 0; i < section.size(); i++){
            
            //individual lines
           String[] main = new String[13];
        
           JsonObject current = (JsonObject)section.get(i);
           JsonObject currentCourse = (JsonObject)course.get(current.get("subjectid").toString().concat(" ").concat(current.get("num").toString()));
           
           JsonArray teachers = (JsonArray)current.get("instructor");
          
           main[0] =  current.get("crn").toString();
           main[1] =  subject.get(current.get("subjectid").toString()).toString();
           main[2] = current.get("subjectid").toString().concat(" ").concat(current.get("num").toString());
           main[3] = currentCourse.get("description").toString();
           main[4] = current.get("section").toString();
           main[5] = current.get("type").toString();
           main[6] = currentCourse.get("credits").toString();
           main[7] = current.get("start").toString();
           main[8] = current.get("end").toString();
           main[9] = current.get("days").toString();
           main[10] = current.get("where").toString();
           main[11] = scheduleType.get(main[5]).toString();
     
           
           //breaks up and formats teachers
           StringBuilder builder = new StringBuilder();
           for(int t = 0; t < teachers.size(); t++){
               if(t ==0){
                   builder.append(teachers.get(t).toString());
               }
               else{
                   builder.append(", ").append(teachers.get(t).toString());
               }
           }
           main[12] = builder.toString();

            result.add(main);
        }
      
        return getCsvString(result); 
        
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