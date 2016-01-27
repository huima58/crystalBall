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
 * RunCrystallBall.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball;

import crystalball.backtest.BackTest;
import crystalball.monitor.ib.IBMonitor;
import crystalball.quant.QuantWekaSMOreg;
import crystalball.util.DateTimeUtil;

/**
 * Helper class that executes CrystalBall from the command line. 
 * There are three command options: histdata, backtest, and realtime.
 * Run with no option to get usage - e.g.<br>
 * <br>
 * java RunCrystallBall <br>
 * <br>
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class RunCrystalBall {

	public static void main(String[] args) {
		IBMonitor[] ibMonitors = new IBMonitor[Config.getInstance().supportedSBLNumber];
		if(args.length > 0){
			if(args[0].equals("histdata")){
				if(args.length <= 2 || args.length > ibMonitors.length + 2) {
					printUsage();
					return;
				}
				String startDate = args[args.length - 1];
				String[] sbls = getSBLsFromArg(args, 1, args.length - 2);
				createIBMonitorsForRequiringHistoricalData(ibMonitors, sbls, startDate);
			} else if(args[0].equals("backtest")){
				if(args.length != 4) {
					printUsage();
					return;
				}
				// Date format is yyyy-MM-dd.
				String startDate = args[2];
				String endDate = args[3];
				String sbl = args[1];
				new BackTest(sbl, startDate, endDate, new QuantWekaSMOreg()).run();
			} else if(args[0].equals("realtime")){
				if(args.length <= 1 || args.length > ibMonitors.length + 1) {
					printUsage();
					return;
				}
				String[] sbls = getSBLsFromArg(args, 1, args.length - 1);
				createIBMonitorsForRealTimeMonitoring(ibMonitors, sbls);
			}
		}
		else {
			printUsage();
		}
	}
	
	public static void createIBMonitorsForRealTimeMonitoring(IBMonitor[] ibMonitors, String[] sbls){
	    int sblId = 0;
	    for (String sbl : sbls) {
	    	boolean existed = false;
	    	for(int i=0; i<sblId; i++){  // remove the redundant candidates.
	    		if(ibMonitors[i].barData.sblName.equals(sbl)) {
	    			existed = true;
	    			break;
	    		}
	    	}
	    	if(existed) continue;
	    	ibMonitors[sblId++] = new IBMonitor(sbl, new QuantWekaSMOreg());
		}
	}
	
	private static String[] getSBLsFromArg(String[] args, int idStart, int idEnd){
	    String[] rst = new String[idEnd - idStart + 1];
	    for (int i = idStart; i <= idEnd; i++) {
	    	rst[i - idStart] = args[i];
		}
	    return rst;
	}

	private static void createIBMonitorsForRequiringHistoricalData(IBMonitor[] ibMonitors, String[] sbls, String startDate){
	    int sblId = 0;
	    for (String sbl : sbls) {
	    	boolean existed = false;
	    	for(int i=0; i<sblId; i++){  // remove the redundant candidates.
	    		if(ibMonitors[i].barData.sblName.equals(sbl)) {
	    			existed = true;
	    			break;
	    		}
	    	}
	    	if(existed) continue;
	    	ibMonitors[sblId++] = new IBMonitor(sbl, DateTimeUtil.strMSShortToDate(startDate));
		}
	}
	
	private static void printUsage(){
		System.out.println("RunCrystalBall [histdata sbl_1 ... sbl_n start_date | backtest sbl start_date end_date | realtime sbl_1 ... sbl_n]");
		System.out.println("Date format is yyyy-MM-dd.");
		System.out.println("Option histdata: get historical data for no more than " + Config.getInstance().supportedSBLNumber + " symbols.");
		System.out.println("Option backtest: backtest for one symbol.");
		System.out.println("Option realtime: realtime monitor no more than " + Config.getInstance().supportedSBLNumber + " symbols.");
		System.out.println("Suppot two types of database: Cassandra and MySQL. Need to specify one during runtime.");
		System.out.println("Database type can be set from system environment variable or -D system properties.");
		System.out.println("For example, to set Cassandra db: DB_TYPE=CASSANDRA CASSANDRA_NODE=mycassandra.com KEYSPACE_NAME=demo");
		System.out.println("             to set MySQL db: DB_TYPE=MYSQL MYSQL_URL=jdbc:mysql://mysql_server/db_name?user=xxx&password=yyy");
		System.out.println("If both system environement and -D properties are set, -D properties take effect.");
	}
	
}
