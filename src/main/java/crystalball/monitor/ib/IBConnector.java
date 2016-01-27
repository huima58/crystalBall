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
 * IBConnector.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.monitor.ib;

import java.util.ArrayList;
import java.util.Random;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;

/**
 * Connect to IB WTS.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class IBConnector implements IConnectionHandler{
	private final Logger m_inLogger = new Logger();
	private final Logger m_outLogger = new Logger();
	private ApiController m_controller;
	public boolean connected = false;
	
	public IBConnector(){
	}
	
    public ApiController getController() {
        if ( m_controller == null ) {
            m_controller = new ApiController( this, m_inLogger, m_outLogger );
        }
        return m_controller;
    }
	
	public void connect(){
        // make initial connection to local host, port 7496, client id 0, no connection options
		// should set a random client id here.
		int clientId = Math.abs(new Random().nextInt(1000) + 1);
		System.out.println("connect to tws server with clientId " + clientId);
		getController().connect( "127.0.0.1", 7496, clientId, null );
	}
	
	public void disconnect(){
	    getController().disconnect();
	}
	
	public void connected() {
		System.out.println("connected in IBConnector");
		connected = true; 
	}

	public void disconnected() {
	}

	public void accountList(ArrayList<String> list) {
	}

	public void message(int id, int errorCode, String errorMsg) {
	}

	public void show(String string) {
		System.out.println(string);
	}

	private static class Logger implements ILogger {
		public Logger() {
		}

		public void log(final String str) {
		}
	}

	public void error(Exception e) {
	}
}
