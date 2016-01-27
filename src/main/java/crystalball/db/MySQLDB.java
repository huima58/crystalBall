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
 * MySQLDB.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.db;

import java.util.LinkedList;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import crystalball.Config;
import crystalball.util.BarData;
import crystalball.util.DateTimeUtil;

/**
 * Access tables in MySQL.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class MySQLDB {
	private static Connection conn_mysql = null;

	/*
	 create table SBL_m5 ( barTime datetime not null, id int, openP decimal(12,4), high decimal(12,4), low decimal(12,4),
	 closeP decimal(12,4), volume int,
     CONSTRAINT SBL_M5_PRIME_KEY PRIMARY KEY (barTime));
	*/
	public static void createM5Table(String sbl){
		LinkedList<String> sqlList = new LinkedList<String>();
        String sqlStr = "create table " + sbl 
				+ "_m5 ( barTime datetime not null, id int, openP decimal(12,4), high decimal(12,4), low decimal(12,4), "
				+ "closeP decimal(12,4), volume int,CONSTRAINT "
				+ sbl +"_M5_PRIME_KEY PRIMARY KEY (barTime))";
        sqlList.add(sqlStr);
        execute(sqlList);
	}
	
    public static void checkM5Table(String sbl)
    {
		if(conn_mysql == null) connect_mysql();
		String sqlStr = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + sbl +"_m5'";
	    try {
	    	ResultSet rs = select(sqlStr, true);
	    	if (!rs.next()) {
	    		createM5Table(sbl);
	    		return;
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
    }

	public static void readM5Data(BarData barData, String startTime, String endTime){
		if(conn_mysql == null) connect_mysql();
		String sqlStr = null;
	    try {
            sqlStr = "select barTime, openP, high, low, closeP, volume from " + barData.sblName + "_m5" + ((startTime != null || endTime != null)?" where":"");
			if(startTime != null) sqlStr += " barTime>='" + startTime + "'"; 
			if(endTime != null) sqlStr += (startTime != null?" and":"") + " barTime<='" + endTime + "'"; 
            sqlStr += " order by barTime desc";
	    	
	    	ResultSet rs = select(sqlStr, true);
	    	rs.last();
	    	barData.dataLen = Math.min(BarData.DataLenLimit - 1000, rs.getRow());
	    	int i = barData.dataLen - 1;
	    	rs.beforeFirst();
	    	while (rs.next()) {
	    		barData.time[i] = DateTimeUtil.strMSToDate(rs.getString("barTime"));
	            barData.close[i] = Double.valueOf(rs.getString("closeP").trim()).doubleValue();
	            barData.open[i] = Double.valueOf(rs.getString("openP").trim()).doubleValue();
	            barData.high[i] = Double.valueOf(rs.getString("high").trim()).doubleValue();
	            barData.low[i] = Double.valueOf(rs.getString("low").trim()).doubleValue();
	            barData.volume[i] = Integer.valueOf(rs.getString("volume").trim()).intValue();
	        	i--;
	        	if(i<0)break;
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
    public static void saveM5Data(BarData barData)
    {
		if(conn_mysql == null) connect_mysql();
		LinkedList<String> sqlList = new LinkedList<String>();
		String strMSTime = null;
		//sometimes IB returns incomplete last bar, always update the last bar.
		if(barData.dataLenBeforeAdd > 0){
			strMSTime = DateTimeUtil.dateToStrMS(barData.time[barData.dataLenBeforeAdd - 1]);
			String sqlStr = "update " + barData.sblName + "_m5 set openP=" + barData.open[barData.dataLenBeforeAdd - 1] 
					+ ",high=" + barData.high[barData.dataLenBeforeAdd - 1] + ",low=" + barData.low[barData.dataLenBeforeAdd - 1] 
					+ ",closeP=" + barData.close[barData.dataLenBeforeAdd - 1] + ",volume=" + barData.volume[barData.dataLenBeforeAdd - 1]	
					+ " where barTime='" + strMSTime + "'";
			sqlList.add(sqlStr);
		}
		execute(sqlList);
		insertM5Data(barData, barData.dataLenBeforeAdd, barData.dataLen - 1);
    }
    
    public static void insertM5Data(BarData barData, int startId, int endId)
    {
		if(startId < 0 || startId > endId || endId >= barData.dataLen) return;
		if(conn_mysql == null) connect_mysql();
		LinkedList<String> sqlList = new LinkedList<String>();
		for(int i=startId; i<=endId; i++){
			String strMSTime = DateTimeUtil.dateToStrMS(barData.time[i]);
			String sqlStr = "insert into " + barData.sblName + "_m5 (barTime, openP, high, low, closeP, volume) values ('" 
							+ strMSTime + "', " + barData.open[i] + ", " + barData.high[i] + ", " + barData.low[i] + ", "
							+ barData.close[i] + ", " + barData.volume[i] + ")";
			sqlList.add(sqlStr);
		}
		execute(sqlList);
    }
    
    public static ResultSet select(String sqlSelect, boolean  resultSetScrollable){
		try{
	        // Create a result set containing all data from my_table
	        Statement stmt = null;
	        if(!resultSetScrollable) stmt = conn_mysql.createStatement();
	        else stmt = conn_mysql.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

	        ResultSet rs = stmt.executeQuery(sqlSelect);
	        return rs;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

    public static void execute(LinkedList<String> sqlList)
    {
        String sqlStr = null;
        try
        {
	        Statement stmt = null;
	        stmt = conn_mysql.createStatement();
	        while(!sqlList.isEmpty())
            {
                sqlStr = sqlList.poll();
                stmt.executeUpdate(sqlStr);
            }
            stmt.close();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        	System.out.println(sqlStr);
        }

    }
	
	public static void connect_mysql(){
		String url = Config.getInstance().getMySqlURL();

		try {
			conn_mysql = DriverManager.getConnection(url); 
			if(conn_mysql == null )	{
				System.out.println("!!!!!!! Cannot get mysql database connection.");
				return;
			}
		}
		catch( java.sql.SQLException e )
		{
			e.printStackTrace();
			return;
		} 	
	}
}
