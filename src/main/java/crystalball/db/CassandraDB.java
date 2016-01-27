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
 * CassandraDB.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.db;

import java.math.BigDecimal;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.ResultSet;

import crystalball.Config;
import crystalball.util.BarData;

/**
 * Access tables in Cassandra.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class CassandraDB {
	private static Cluster cluster;
	private static Session session;
	   
	/* 
	 * create table stock_m5(symbol varchar, barTime timestamp, open decimal, high decimal, low decimal, close decimal, volume bigint, 
       primary key((symbol), barTime))
       with clustering order by (barTime asc);
	 *
	 */
	public static void createM5Table(){
		if(session == null) connectCassandra();
        String cqlStr = "create table stock_m5(symbol varchar, barTime timestamp, open decimal, high decimal, low decimal, close decimal, "
        		+ "volume bigint, primary key((symbol), barTime)) with clustering order by (barTime asc)";
        session.execute(cqlStr);
	}
    public static void checkM5Table()
    {
		if(session == null) connectCassandra();
    	KeyspaceMetadata ks = cluster.getMetadata().getKeyspace(Config.getInstance().getKeySpaceName());
    	TableMetadata table = ks.getTable("stock_m5");
    	if(table == null) {
    		createM5Table();
    	}
    }
	public static void readM5Data(BarData barData, String startTime, String endTime){
		if(session == null) connectCassandra();
        String cqlStr = "SELECT barTime, open, high, low, close, volume FROM stock_m5 WHERE symbol='" + barData.sblName + "'";
        if(startTime != null) cqlStr += " and barTime>='" + startTime + "'"; 
        if(endTime != null) cqlStr += " and barTime<='" + endTime + "'"; 
        cqlStr += " order by barTime";
        ResultSet results = session.execute(cqlStr);
        int i = 0;
        for (Row row : results) {
            barData.time[i] = row.getDate("barTime");
            barData.open[i] =  row.getDecimal("open").doubleValue();
            barData.high[i] = row.getDecimal("high").doubleValue();
            barData.low[i] = row.getDecimal("low").doubleValue();
            barData.close[i] = row.getDecimal("close").doubleValue();
            barData.volume[i] = row.getLong("volume");
            i++;
        }
        barData.dataLen = i;
	}
	
    public static void insertM5Data(BarData barData, int startId, int endId)
    {
		if(startId < 0 || startId > endId || endId >= barData.dataLen) return;
		if(session == null) connectCassandra();
		// Prepare the statements
		PreparedStatement insert_stmt = session.prepare("INSERT INTO stock_m5(symbol, barTime, open, high, low, close, volume) VALUES (?, ?, ?, ?, ?, ?, ?)");
		// Add the prepared statements to a batch
		BatchStatement batch = new BatchStatement();
		for(int i=startId; i<=endId; i++) {
			batch.add(insert_stmt.bind(barData.sblName, barData.time[i], 
					new BigDecimal(barData.open[i]).setScale(2, BigDecimal.ROUND_HALF_UP), new BigDecimal(barData.high[i]).setScale(2, BigDecimal.ROUND_HALF_UP), 
					new BigDecimal(barData.low[i]).setScale(2, BigDecimal.ROUND_HALF_UP), 
					new BigDecimal(barData.close[i]).setScale(2, BigDecimal.ROUND_HALF_UP), barData.volume[i]));
		}
		// Execute the batch
		session.execute(batch);
    }
    
    public static void updateM5Data(BarData barData, int startId, int endId)
    {
		if(startId < 0 || startId > endId || endId >= barData.dataLen) return;
		if(session == null) connectCassandra();
		// Prepare the statements
		PreparedStatement insert_stmt = session.prepare("Update stock_m5 SET open=?, high=?, low=?, close=?, volume=? where symbol='"
				+ barData.sblName + "' AND barTime=?");
		// Add the prepared statements to a batch
		BatchStatement batch = new BatchStatement();
		for(int i=startId; i<=endId; i++) {
			batch.add(insert_stmt.bind( 
					new BigDecimal(barData.open[i]).setScale(2, BigDecimal.ROUND_HALF_UP), new BigDecimal(barData.high[i]).setScale(2, BigDecimal.ROUND_HALF_UP), 
					new BigDecimal(barData.low[i]).setScale(2, BigDecimal.ROUND_HALF_UP), 
					new BigDecimal(barData.close[i]).setScale(2, BigDecimal.ROUND_HALF_UP), barData.volume[i],
					barData.time[i]));
		}
		// Execute the batch
		session.execute(batch);
    }
    
    public static void saveM5Data(BarData barData)
    {
		if(session == null) connectCassandra();
		//sometimes IB returns incomplete last bar, always update the last bar.
		if(barData.dataLenBeforeAdd > 0){
			updateM5Data(barData, barData.dataLenBeforeAdd - 1, barData.dataLenBeforeAdd - 1);
		}
		insertM5Data(barData, barData.dataLenBeforeAdd, barData.dataLen - 1);
    }
    
	public static void connectCassandra(){
		if(session != null) return;
		try {
			cluster = Cluster.builder().addContactPoint(Config.getInstance().getCassandraNode()).build();
			session = cluster.connect(Config.getInstance().getKeySpaceName());
		}
		catch( Exception e )
		{
			e.printStackTrace();
			if(cluster != null) cluster.close();
			return;
		} 	
	}
}