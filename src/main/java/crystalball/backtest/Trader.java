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
 * Trader.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.backtest;

import crystalball.quant.Quant;
import crystalball.quant.QuantDecision;
import crystalball.quant.QuantDecision.ActionSuggestion;
import crystalball.util.BarData;
import crystalball.util.DateTimeUtil;

/**
 * Trade according to the Quant's decision.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class Trader {
	private BarData barData;
	private Quant quant;
	private PositionState positionState;
	private Statistic statistic = new Statistic();
	public enum Action {Buy, Sell, Clear};
	private static final double commisionUnit = 0;
	
	public Trader(BarData barData, PositionState positionState, Quant quant){
		this.barData = barData;
		this.positionState = positionState;
		this.quant = quant;
	}
	
	public void trade(){
		for(int i=0; i<barData.dataLen; i++){
			QuantDecision quantDecision = quant.makeDecision(barData, i, positionState);
			if(quantDecision == null) continue;
			if(positionState.position > 0){
				if(quantDecision.suggestion != ActionSuggestion.Long 
					&& quantDecision.suggestion != ActionSuggestion.HoldLong
					&& quantDecision.suggestion != ActionSuggestion.Neutral){
					trade(i, Action.Clear);
				}
			}else if(positionState.position < 0){
				if(quantDecision.suggestion != ActionSuggestion.Short
					&& quantDecision.suggestion != ActionSuggestion.HoldShort
					&& quantDecision.suggestion != ActionSuggestion.Neutral){
					trade(i, Action.Clear);
				}
			}else{
				if(quantDecision.suggestion == ActionSuggestion.Short){
					trade(i, Action.Sell);
				}else if(quantDecision.suggestion == ActionSuggestion.Long){
					trade(i, Action.Buy);
				}
			}
		}
		statistic.print();
	}
	
	public void trade(int id,  Action action){
		if( (action == Action.Buy && positionState.position < 0)
			|| (action == Action.Sell && positionState.position > 0)){
			trade(id, Action.Clear);
		}
		if(action == Action.Clear){
			if(positionState.position == 0) return;
			if(positionState.position > 0) clearLongPosition(id, barData.close[id]);
			else clearShortPosition(id, barData.close[id]);
			return;
		}
		
		if(action == Action.Buy || action == Action.Sell) 
			if(positionState.cash <= (barData.close[id] + commisionUnit)) return;  // At least buy / sell one share.
		
		int share = (int) (positionState.cash / (barData.close[id] + commisionUnit));
		if(action == Action.Buy) {
			tradeLong(id, barData.close[id], share);
		}else if(action == Action.Sell){
			tradeShort(id, barData.close[id], share);
		}
	}
	
	private void tradeLong(int id, double price, double position){
		positionState.position += position;
		positionState.cash -= positionState.position * (price + commisionUnit);
		positionState.enterBar = id;
		positionState.enterPrice = price + commisionUnit;
	}

	private void clearLongPosition(int id, double price){
		if(positionState.position <= 0) return; 
		double earning = positionState.position * (price - commisionUnit - positionState.enterPrice);
		positionState.cash += positionState.position * (price - commisionUnit);
		printTrade(positionState, id, earning);
		statistic.addContract(earning, positionState.position, id - positionState.enterBar);
		positionState.position = 0;
	}

	private void tradeShort(int id, double price, double position){
		positionState.position -= position;
		positionState.cash += positionState.position * price;
		positionState.enterBar = id;
		positionState.enterPrice = price - commisionUnit;
	}

	private void clearShortPosition(int id, double price){
		if(positionState.position >= 0) return; 
		double earning = (-positionState.position) * (positionState.enterPrice - price - commisionUnit);
		positionState.cash += earning + (-positionState.position) * (positionState.enterPrice + commisionUnit);
		printTrade(positionState, id, earning);
		statistic.addContract(earning, positionState.position, id - positionState.enterBar);
		positionState.position = 0;
	}

	private void printTrade(PositionState positionState, int clearBarId, double earning){
		System.out.format("quantity: %4.0f %20s %20s  price: %6.2f sellPrice: %6.2f total cash: %8.2f earning:%8.2f%n", positionState.position, DateTimeUtil.dateToStrMS(barData.time[positionState.enterBar]), 
			DateTimeUtil.dateToStrMS(barData.time[clearBarId]), positionState.enterPrice, 
			barData.close[clearBarId], positionState.cash, earning);
	}
}
