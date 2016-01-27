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
 * QuantDecision.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.quant;

/**
 * Decide a quant suggestion.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class QuantDecision {
	public enum ActionSuggestion {Long, Short, HoldLong, HoldShort, Neutral};
	public ActionSuggestion suggestion;
	
	public String toString(){
	    if(suggestion != null) return suggestion.toString();
	    else return "";
	}
}
