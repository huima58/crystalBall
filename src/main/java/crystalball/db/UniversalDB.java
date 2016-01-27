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
 * UniversalDB.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.db;

import crystalball.Config;
import crystalball.Config.DBType;
import crystalball.util.BarData;

/**
 * A middle layer DB that selects one supported DB.
 * No Factory pattern here because only one DB is supported at a time.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class UniversalDB {
    private boolean checkedTable = false;
	
	public void getHistoricalData(BarData barData, String startTime, String endTime){
	    checkTable(barData);
		if(Config.getInstance().dbType() == DBType.CASSANDRA) {
			CassandraDB.readM5Data(barData, startTime, endTime);
		}else if (Config.getInstance().dbType() == DBType.MYSQL) {
			MySQLDB.readM5Data(barData, startTime, endTime);
		}
	}

	public void saveHistoricalData(BarData barData){
        checkTable(barData);
		if(Config.getInstance().dbType() == DBType.CASSANDRA) {
			CassandraDB.saveM5Data(barData);
		}else if (Config.getInstance().dbType() == DBType.MYSQL) {
			MySQLDB.saveM5Data(barData);
		}
	}
	
	public void checkTable(BarData barData){
	    if(checkedTable) return;
        if(Config.getInstance().dbType() == DBType.CASSANDRA) {
            CassandraDB.checkM5Table();
        }else if (Config.getInstance().dbType() == DBType.MYSQL) {
            MySQLDB.checkM5Table(barData.sblName);
        }
        checkedTable = true;
	}
}
