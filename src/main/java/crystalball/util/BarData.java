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
 * BarData.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.util;

import java.util.Calendar;
import java.util.Date;
import crystalball.db.UniversalDB;

/**
 * Stores historical data.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class BarData {
	public String sblName = null;
	public int dataLen = 0;
	public int dataLenBeforeAdd = -1;
	public static int DataLenLimit = 200000;
	public double[] close = new double[DataLenLimit];
	public double[] open = new double[DataLenLimit];
	public double[] high = new double[DataLenLimit];
	public double[] low = new double[DataLenLimit];
	public long[] volume = new long[DataLenLimit];
	// Date consumes 32 bytes, String occupies 36 + length*2 bytes aligned by 8 bytes
	public Date[] time = new Date[DataLenLimit]; 
	
	public BarData(String sblName){
		this.sblName = sblName;
	}

	public BarData(String sblName, String strStartDate, String strEndDate){
		this.sblName = sblName;
		new UniversalDB().getHistoricalData(this, strStartDate, strEndDate);
	}

	public void addRow(String time, double open, double close, double high, double low, long volume){
		// Sometimes it may overlap
		if(dataLen > 0){
			Date lastTime = this.time[dataLen - 1];  
			Date curTime = DateTimeUtil.strMSToDate(time);
			if(curTime.compareTo(lastTime) < 0) return;
			else if(curTime.compareTo(lastTime) == 0){ //sometimes IB returns incomplete last bar, always update the last bar.
				dataLen--;
			}
		}
		this.time[dataLen] = DateTimeUtil.strIBToDate(time);
		this.high[dataLen] = high;
		this.low[dataLen] = low;
		this.open[dataLen] = open;
		this.close[dataLen] = close;
		this.volume[dataLen] = volume;
		dataLen++;
	}
	
	// IB sometimes returns an incomplete last bar.
	// Check if the last 5_minutes bar is incomplete or not.
	public boolean checkIfLastBarComplete(){
		Calendar cal = Calendar.getInstance();
		Date d1 = cal.getTime();
		Date d2 = time[dataLen - 1];
		// Get msec from each, and subtract.
		long diffSeconds = (d1.getTime() - d2.getTime()) / 1000;
		if(diffSeconds >= 300) return true;
		else return false;
	}
	
}
