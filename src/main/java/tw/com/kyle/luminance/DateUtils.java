/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.DateTools;

/**
 *
 * @author Sean_S325
 */
public class DateUtils {
    public static String now(){
        return DateTools.dateToString(new Date(), DateTools.Resolution.SECOND);
    }
    
    public static String buildDate(String date_str) {
        DateFormat format = null;
        if(date_str.contains(":")){
            format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); 
            format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        } else if (date_str.contains("+") || date_str.contains("-")){
            format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ssZ");
        } else {
            format = new SimpleDateFormat("yyyy/MM/dd");
        }
        
        try {
            Date date_obj = format.parse(date_str);            
            return DateTools.dateToString(date_obj, DateTools.Resolution.SECOND);
        } catch (ParseException ex) {
            Logger.getLogger(DateUtils.class.getName()).log(Level.SEVERE, null, ex);
            return now();
        }        
    }
}
