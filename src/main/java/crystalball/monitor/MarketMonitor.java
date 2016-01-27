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
 * MarketMonitor.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.monitor;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import crystalball.db.UniversalDB;
import crystalball.quant.Quant;
import crystalball.quant.QuantDecision;
import crystalball.util.BarData;
import crystalball.util.DateTimeUtil;

/**
 * Abstract class to acquire market data.
 * Now only support IB APIs.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public abstract class MarketMonitor {
	public static final int DELAY_SECONDS_TO_ACQUIRE_DATA_AFTER_START = 2;
    private static int monitorNum = 0;
	
    private Timer timer = new Timer();
    public BarData barData = null; 
    
	protected boolean connected = false;
	private boolean dataReady = false;
	
	private Quant quant = null;
	
	private Date dataStartDate;
	private UniversalDB db = new UniversalDB();
	
	public abstract void reqMarketData();
	public abstract void reqMarketData(String endDateTime);
	// Implement the following functions if necessary.
    public abstract void lockConnector();
	public abstract void unlockConnector();
	// quit if monitorNum is 0.
    public abstract void quit();
	
	public MarketMonitor(String sblName, Quant quant){
		barData = new BarData(sblName);
		this.quant = quant;
		Date taskExecutionTime = getTaskExecutionTime(true);
        monitorNum++;
        timer.schedule(new MonitorTask(), taskExecutionTime);
	}
	
	// No quant, it is for acquiring historical data only
	public MarketMonitor(String sblName, Date dataStartDate){
		barData = new BarData(sblName);
		this.dataStartDate = dataStartDate;
		
		// Get database data
		db.getHistoricalData(barData, null, null);
		Date taskExecutionTime = getTaskExecutionTime(true);
        monitorNum++;
        timer.schedule(new MonitorTaskForAcquiringHistoricalData(), taskExecutionTime);
	}
	
	// Called by the thread that acquires the data
	public void setDataReady(){
		dataReady = true;
	}
	
	public Date getTaskExecutionTime(boolean isFirstTime){
	    TimeZone timeZone = TimeZone.getTimeZone("UTC");
	    Calendar calendar = Calendar.getInstance(timeZone);
	    
		if(isFirstTime){
			calendar.add(Calendar.SECOND, DELAY_SECONDS_TO_ACQUIRE_DATA_AFTER_START);
			return calendar.getTime();
		}
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		
        // UTC market time: from 14:30 to 21:00
		if(hour < 14 || (hour == 14 && minute < 35)) {
			calendar.set(Calendar.HOUR_OF_DAY, 14);
			calendar.set(Calendar.MINUTE, 35);
			calendar.set(Calendar.SECOND, 2);
		}else if(hour >= 21){
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 14);
			calendar.set(Calendar.MINUTE, 35);
			calendar.set(Calendar.SECOND, 2);
		}else if(minute >= 55){
			calendar.set(Calendar.HOUR_OF_DAY, hour + 1);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 2);
		}else {
			calendar.set(Calendar.MINUTE, (((int) (minute / 5)) + 1) * 5);
			calendar.set(Calendar.SECOND, 2);
		}
		
		return calendar.getTime();
	}
	
    class MonitorTask extends TimerTask {
        private void realTimeDecision(){
            QuantDecision decision = null;
            int lastCompleteBarId = barData.dataLen - 1;
            // Check if the last bar is complete. Sometimes IB returns incomplete last bar.
            if(!barData.checkIfLastBarComplete())
                lastCompleteBarId = barData.dataLen - 2;
            decision = quant.makeDecision(barData, lastCompleteBarId, null);
            System.out.println(barData.time[lastCompleteBarId] + " " 
                    + (decision == null?"null":decision.toString()));
        }
        
        public void run() {
			dataReady = false;
			barData.dataLenBeforeAdd = barData.dataLen;
			lockConnector();
    		reqMarketData();
    		
    		int waitLimit = 10;
    		int waitNum = 0;
    		// wait for the data ready
    		while (true) {
    			if(!connected) {
    				System.out.println("not connected yet.");
    				break;
    			}
    			if(dataReady) {
    				unlockConnector();
    				if(barData.dataLen >= 2 && quant != null) {
    				    realTimeDecision();
    				}
    				dataReady = false;
    				break;
    			}
    			try{ 
    				waitNum++;
    				if(waitNum >= waitLimit) {
    					System.out.println(barData.sblName + " cannot receive stock data.");
    					unlockConnector();
    					break;
    				}
    				Thread.sleep(5000);
    			}catch(Exception e){e.printStackTrace();}
    		}
    		
    		// schedule next event time
    		Date taskExecutionTime = getTaskExecutionTime(false);
    		timer.schedule(new MonitorTask(), taskExecutionTime);
        }
    }

    class MonitorTaskForAcquiringHistoricalData extends TimerTask {
        public void run() {
			dataReady = false;
			// If table has data, set dataStartDate as the table end date
			if(barData.dataLen > 0) {
				dataStartDate = barData.time[barData.dataLen - 1];
			}
			Date newEndDate = DateTimeUtil.dateAdd(dataStartDate, Calendar.DATE, 5);
			
			barData.dataLenBeforeAdd = barData.dataLen;
            lockConnector();
			reqMarketData(DateTimeUtil.dateToStrIBInput(newEndDate));
    		
    		int waitLimit = 50;
    		int waitNum = 0;
    		// wait for the data ready
    		while (true) {
    			if(!connected) {
    				System.out.println("not connected yet.");
    				break;
    			}
    			if(dataReady) {
    				unlockConnector();
    	    		// save data
    	    		if(barData.dataLen != 0){
    	    			db.saveHistoricalData(barData);
    	    		}
    				dataReady = false;
    				break;
    			}
    			try{ 
    				waitNum++;
    				if(waitNum >= waitLimit) {
    					System.out.println(barData.sblName + " cannot receive stock data.");
    					unlockConnector();
    					barData.dataLen = 0;
    					break;
    				}
    				Thread.sleep(6000);
    			}catch(Exception e){e.printStackTrace();}
    		}
    		
    		if(barData.dataLen > barData.dataLenBeforeAdd) {
                System.out.println(barData.sblName + " from " 
                        + (barData.dataLenBeforeAdd>=1?barData.time[barData.dataLenBeforeAdd - 1]:barData.time[0]) 
                        + " to " + barData.time[barData.dataLen - 1] );
	    		// schedule next event time
	    		Date taskExecutionTime = getTaskExecutionTime(true);
	    		timer.schedule(new MonitorTaskForAcquiringHistoricalData(), taskExecutionTime);
    		}else{
    			System.out.println(barData.sblName + " done acquiring history data.");
    			timer.cancel();
    			timer.purge();
    			monitorNum--;
    			if(monitorNum <= 0) quit();
    		}
        }
    }
}
