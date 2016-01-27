/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * DateTimeUtil.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utilities for DateTime operation.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class DateTimeUtil {
	// IB receives one type of date format but returns another type.
	private static DateFormat formatterIBInput = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private static DateFormat formatterIB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // For MySQL format
	private static DateFormat formatterMSShort = new SimpleDateFormat("yyyy-MM-dd");
	private static DateFormat formatterMS = formatterIB;
        
    public static Date strIBToDate(String str_date) {
        try {
            return (Date)formatterIB.parse(str_date);
        } catch (ParseException e) {
            System.out.println("StrToDateIB Exception :"+e + " " + str_date);
            return null;
        }
    }

    public static Date strMSToDate(String str_date) {
        try {
            return (Date)formatterMS.parse(str_date);
        } catch (ParseException e) {
            System.out.println("StrToDateSS Exception :"+e + " " + str_date);
            return null;
        }
    }

    public static Date strMSShortToDate(String str_date) {
        try {
            return (Date)formatterMSShort.parse(str_date);
        } catch (Exception e) {
            System.out.println("StrToDateSSShort Exception :"+e + " " + str_date);
            return null;
        }
    }
        
    public static String dateToStrMS(Date date) {
        try {
            return formatterMS.format(date);
        } catch (Exception e) {
            System.out.println("DateToStrMS Exception :"+e + " " + date);
            return null;
        }
    }
    
    public static String dateToStrIB(Date date) {
        try {
            return formatterIB.format(date);
        } catch (Exception e) {
            System.out.println("DateToStrIB Exception :"+e + " " + date);
            return null;
        }
    }

    public static String dateToStrIBInput(Date date) {
        try {
            return formatterIBInput.format(date);
        } catch (Exception e) {
        	System.out.println("DateToStrIBInput Exception :"+e + " " + date);
        	return null;
        }
    }

    public static Date dateAdd(Date date, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(date);
            calendar.add(field, amount);
            return calendar.getTime();
        } catch (Exception e) {
            System.out.println("DateAdd Exception :"+e);
             return null;
        }
    }

    public static String getCurrentTimeZone(){
        Calendar cal = Calendar.getInstance();
        long milliDiff = cal.get(Calendar.ZONE_OFFSET);
        String [] ids = TimeZone.getAvailableIDs();
        String name = null;
        for (String id : ids) {
          TimeZone tz = TimeZone.getTimeZone(id);
          if (tz.getRawOffset() == milliDiff) {
            name = id;
            break;
          }
        }
        return name;
    }
}
