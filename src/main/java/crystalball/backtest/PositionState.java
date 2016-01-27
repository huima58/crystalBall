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
 * PositionState.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.backtest;

/**
 * Indicate the cash and position state.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class PositionState {
	public double cash = 0;
	public double position = 0; // < 0 for short
	
	public int enterBar = 0;
	public double enterPrice;

	public PositionState(double cash){
		this.cash = cash;
	}
}