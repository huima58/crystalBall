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
 * Statistic.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.backtest;

/**
 * Count the statistic from transactions.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class Statistic {
	public double initialCapital = 0;
	public double sumEarningLong = 0, sumEarningLongWin = 0, sumEarningLongLoss = 0;
	public double sumEarningShort = 0, sumEarningShortWin = 0, sumEarningShortLoss = 0;
	public double numLongWin = 0, numLongLoss = 0, numLongEven = 0; 
	public double numShortWin = 0, numShortLoss = 0, numShortEven = 0;
	public double curDrawDown = 0, maxDrawDown = 0;
	public double curConsecutiveWinningTrades = 0, maxConsecutiveWinningTrades = 0;
	public double curConsecutiveLosingTrades = 0, maxConsecutiveLosingTrades = 0;
	public double sumHoldingBarsInWinningTrade = 0, sumHoldingBarsInLosingTrade = 0, sumHoldingBarsInEvenTrade = 0;
	public double largestWinningTrade = 0, largestLosingTrade = 0;
	public double commissionLong = 0, commissionShort = 0;
	
	public void addContract(double earning, double position, int holdingBars){
		boolean isLong = position > 0;
		if(isLong) {
			commissionLong += position * 0.02; 
			sumEarningLong += earning;
			if(earning > 0) {
				sumEarningLongWin += earning;
				numLongWin++;
			}else if(earning < 0) {
				sumEarningLongLoss -= earning;
				numLongLoss++;
			}else numLongEven++;
		} else {
			commissionShort -= position * 0.02; 
			sumEarningShort += earning;
			if(earning > 0) {
				sumEarningShortWin += earning;
				numShortWin++;
			}else if(earning < 0) {
				sumEarningShortLoss -= earning;
				numShortLoss++;
			}else numShortEven = 0;
		}
		if(earning > 0){
			curDrawDown = 0;

			curConsecutiveWinningTrades++;
			if(curConsecutiveWinningTrades > maxConsecutiveWinningTrades) maxConsecutiveWinningTrades = curConsecutiveWinningTrades;

			curConsecutiveLosingTrades = 0;
			
			sumHoldingBarsInWinningTrade += holdingBars;
			
			if(earning > largestWinningTrade) largestWinningTrade = earning;
		}else if(earning < 0){
			curDrawDown -= earning;
			if(curDrawDown > maxDrawDown) maxDrawDown = curDrawDown;

			curConsecutiveWinningTrades = 0;
			
			curConsecutiveLosingTrades++;
			if(curConsecutiveLosingTrades > maxConsecutiveLosingTrades) maxConsecutiveLosingTrades = curConsecutiveLosingTrades;
			
			sumHoldingBarsInLosingTrade += holdingBars;

			if(earning < largestLosingTrade) largestLosingTrade = earning;
		}else {
			sumHoldingBarsInEvenTrade += holdingBars;
		}
	}

	public void print(){
		// Total Net Profit
		// 		Gross Profit
		//		Gross Loss
		//      Profit Factor 
		// --------------------
		// Total Number of Trades
		// 		Winning Trades
		// 		Losing Trades
		//		Even Trades
		// Percent Profitable
		// Avg. Trade Net Profit
		// Avg. Winning Trade Profit
		// Avg. Losing Trade Lost
		// Largest Winning Trade
		// Largest Losing Trade
		// Max Consecutive Winning Trades
		// Max Consecutive Losing Trades
		// Avg. Bars in Winning Trades
		// Avg. Bars in Losing Trades
		// Avg. Bars in Even Trades
		// Max DrawDown
		System.out.format("%50s%18s%20s%n", "Total", "Long", "Short");
		double sumEarning = sumEarningLong + sumEarningShort;
		double sumEarningWin = sumEarningLongWin + sumEarningShortWin;
		double sumEarningLoss = sumEarningLongLoss + sumEarningShortLoss;
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Total Net Profit:"," ", sumEarning, sumEarningLong, sumEarningShort);
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Gross Profit:"," ", (sumEarningLongWin + sumEarningShortWin), sumEarningLongWin, sumEarningShortWin);
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Gross Loss:"," ", (sumEarningLongLoss + sumEarningShortLoss), sumEarningLongLoss, sumEarningShortLoss);
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Profit Factor:"," ",
				((sumEarningLongLoss + sumEarningShortLoss)==0?0:(sumEarningLongWin + sumEarningShortWin)/(sumEarningLongLoss + sumEarningShortLoss)), 
				(sumEarningLongLoss==0?0:(sumEarningLongWin/sumEarningLongLoss)),
				(sumEarningShortLoss==0?0:(sumEarningShortWin/sumEarningShortLoss)));
		System.out.println("-----------------------------------------------------------------------------------------------------------");
		double num = numLongWin + numLongLoss + numLongEven + numShortWin + numShortLoss + numShortEven;
		double numLong = numLongWin + numLongLoss + numLongEven;
		double numShort = numShortWin + numShortLoss + numShortEven;
		double numWin = numLongWin + numShortWin;
		double numLoss = numLongLoss + numShortLoss;
		double numEven = numLongEven + numShortEven;
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Total Number of Trades:"," ", num, numLong, numShort);
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Winning Trades:"," ", (numLongWin + numShortWin), numLongWin, numShortWin);
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Losing Trades:"," ", (numLongLoss + numShortLoss), numLongLoss, numShortLoss);
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Even Trades:"," ", (numLongEven + numShortEven), numLongEven, numShortEven);
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Percent Profitable:"," ", (num==0?0:((numLongWin+numShortWin)/num)), 
				(numLong==0?0:(numLongWin/numLong)), (numShort==0?0:(numShortWin/numShort)));
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Avg. Trade Net Profit:"," ", (num==0?0:(sumEarning/num)), 
				(numLong==0?0:(sumEarningLong/numLong)), (numShort==0?0:(sumEarningShort/numShort)));
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Avg. Winning Trade Profit:"," ", (numWin==0?0:(sumEarningWin/numWin)), 
				(numLongWin==0?0:(sumEarningLongWin/numLongWin)), (numShortWin==0?0:(sumEarningShortWin/numShortWin)));
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Avg. Losing Trade Lost:"," ", (numLoss==0?0:(sumEarningLoss/numLoss)), 
				(numLongLoss==0?0:(sumEarningLongLoss/numLongLoss)), (numShortLoss==0?0:(sumEarningShortLoss/numShortLoss)));
		System.out.format("%32s%8s%17.2f%n", "Largest Winning Trade:"," ", largestWinningTrade);
		System.out.format("%32s%8s%17.2f%n", "Largest Losing Trade:"," ", largestLosingTrade);
		System.out.format("%32s%8s%17.2f%n", "Max Consecutive Winning Trades:"," ", maxConsecutiveWinningTrades);
		System.out.format("%32s%8s%17.2f%n", "Max Consecutive Losing Trades:"," ", maxConsecutiveLosingTrades);
		System.out.format("%32s%8s%17.2f%n", "Avg. Bars in Winning Trades:"," ", (numWin==0?0:(sumHoldingBarsInWinningTrade / numWin)));
		System.out.format("%32s%8s%17.2f%n", "Avg. Bars in Losing Trades:"," ", (numLoss==0?0:(sumHoldingBarsInLosingTrade / numLoss)));
		System.out.format("%32s%8s%17.2f%n", "Avg. Bars in Even Trades:"," ", (numEven==0?0:(sumHoldingBarsInEvenTrade / numEven)));
		System.out.format("%32s%8s%17.2f%n", "Max DrawDown:"," ", maxDrawDown);
		System.out.format("%32s%8s%17.2f%18.2f%19.2f%n", "Total Commission:"," ", (commissionLong + commissionShort), commissionLong, commissionShort);
	}
}
