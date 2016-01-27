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
 * BackTest.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.backtest;

/**
 * BackTest class.
 * Quant should be defined by caller.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

import crystalball.quant.Quant;
import crystalball.util.BarData;

public class BackTest implements Runnable {
	private Trader trader;

	private BarData barData;
	private PositionState positionState = new PositionState(100000);
	
	public BackTest(String sblName, String strStartDate, String strEndDate, Quant quant){
		barData = new BarData(sblName, strStartDate, strEndDate);
		trader = new Trader(barData, positionState, quant);
	}

	public void run() {
		trader.trade();
	}
	
}
