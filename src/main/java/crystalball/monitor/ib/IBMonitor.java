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
 * IBMonitor.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.monitor.ib;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ib.client.Contract;
import com.ib.client.Types;
import com.ib.client.Types.SecType;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.Bar;

import crystalball.monitor.MarketMonitor;
import crystalball.monitor.ib.IBConnector;
import crystalball.quant.Quant;
import crystalball.util.DateTimeUtil;

/**
 * Acquire IB market data.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class IBMonitor extends MarketMonitor implements IHistoricalDataHandler {
	// IBMonitors share one IBConnector
	// Only 8 clients can connect to TWS simultaneously. So we have to share connector anyway.
	// Need to lock the connector to acquire historical data. 
	private final static IBConnector ibConnector = new IBConnector();
	private final static Lock ibConnectorlock = new ReentrantLock();
	static { ibConnector.connect(); }
	
	private final Contract m_contract = new Contract();

	public IBMonitor(String sblName, Quant predictor){
		super(sblName.toUpperCase(), predictor);
		m_contract.symbol(this.barData.sblName);
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" );
		m_contract.currency("USD");
	}

	// No quant, it is for acquiring historical data only
	public IBMonitor(String sblName, Date dataStartDate){
		super(sblName.toUpperCase(), dataStartDate);
		m_contract.symbol(this.barData.sblName);
		m_contract.secType( SecType.STK ); 
		m_contract.exchange( "SMART" );
		m_contract.currency("USD");
	}
	
	public String getNowAsEndDateTime(){
		Calendar cal = Calendar.getInstance();
		// IB accepts one format time but send out another format.
		return DateTimeUtil.dateToStrIBInput(cal.getTime()); 
	}
	
	public void reqHistoricalData(){
		reqHistoricalData(getNowAsEndDateTime());
	}

	public void reqHistoricalData(String endDateTime){
		// Use the format yyyymmdd hh:mm:ss tmz, where the time zone is allowed (optionally) after a space at the end.
		// Do not take CDT, only take America/Bahia_Banderas.
		String endDateTimeWithTimeZone = endDateTime + " " + DateTimeUtil.getCurrentTimeZone();
		int duration = 1;
		Types.DurationUnit durationUnit = Types.DurationUnit.WEEK;
		Types.BarSize barSize = Types.BarSize._5_mins;
		Types.WhatToShow whatToShow = Types.WhatToShow.TRADES;
		boolean useRTH = true;  //0: all data 1: only regular trading time
		ibConnector.getController().reqHistoricalData(m_contract, endDateTimeWithTimeZone, duration, durationUnit, barSize, whatToShow, useRTH, this);
	}

	public void historicalData(Bar bar, boolean hasGaps) {
		this.barData.addRow(bar.formattedTime(), bar.open(), bar.close(), bar.high(), bar.low(), bar.volume());
	}

	public void historicalDataEnd() {
		setDataReady();
	}

	@Override
	public void reqMarketData() {
		if(ibConnector.connected) {
			connected = true;
			reqHistoricalData();
		}else connected = false;
	}

	@Override
	public void reqMarketData(String endDateTime) {
		if(ibConnector.connected) {
			connected = true;
			reqHistoricalData(endDateTime);
		}else connected = false;
	}

    @Override
    public void lockConnector() {
        ibConnectorlock.lock();
    }
    
	@Override
	public void unlockConnector() {
		ibConnectorlock.unlock();
	}

    @Override
    public void quit() {
        ibConnector.disconnect();
    }
}
