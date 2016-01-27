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
 * Config.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball;

/**
 * Global configuration class.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class Config {
    private static Config instance = null;
	protected Config() {
		dbTypeSystemProperty = System.getProperty("DB_TYPE");
		if(dbTypeSystemProperty != null) {
			System.out.println("Get system property DB_TYPE=" + dbTypeSystemProperty);
			dbType = dbTypeSystemProperty;
		}else {
			dbTypeSystemEnv = System.getenv("DB_TYPE");
			if(dbTypeSystemEnv != null) {
				System.out.println("Get system env DB_TYPE=" + dbTypeSystemEnv);
				dbType = dbTypeSystemEnv;
			}
		}
		
		mySqlURLSystemProperty = System.getProperty("MYSQL_URL");
		if(mySqlURLSystemProperty != null) {
			System.out.println("Get system property MYSQL_URL=" + mySqlURLSystemProperty);
			mySqlURL = mySqlURLSystemProperty;
		}else {
			mySqlURLSystemEnv = System.getenv("MYSQL_URL");
			if(mySqlURLSystemEnv != null) {
				System.out.println("Get system env MYSQL_URL=" + mySqlURLSystemEnv);
				mySqlURL = mySqlURLSystemEnv;
			}
		}

		cassandraNodeSystemProperty = System.getProperty("CASSANDRA_NODE");
		if(cassandraNodeSystemProperty != null) {
			System.out.println("Get system property CASSANDRA_NODE=" + cassandraNodeSystemProperty);
			cassandraNode = cassandraNodeSystemProperty;
		}else {
			cassandraNodeSystemEnv = System.getenv("CASSANDRA_NODE");
			if(cassandraNodeSystemEnv != null) {
				System.out.println("Get system env CASSANDRA_NODE=" + cassandraNodeSystemEnv);
				cassandraNode = cassandraNodeSystemEnv;
			}
		}
		
		keySpaceNameSystemProperty = System.getProperty("KEYSPACE_NAME");
		if(keySpaceNameSystemProperty != null) {
			System.out.println("Get system property KEYSPACE_NAME=" + keySpaceNameSystemProperty);
			keySpaceName = keySpaceNameSystemProperty;
		}else {
			keySpaceNameSystemEnv = System.getenv("KEYSPACE_NAME");
			if(keySpaceNameSystemEnv != null) {
				System.out.println("Get system env KEYSPACE_NAME=" + keySpaceNameSystemEnv);
				keySpaceName = keySpaceNameSystemEnv;
			}
		}
		
		decideDBType();
	}
	
	public static Config getInstance() {
		if(instance == null) {
	    	instance = new Config();
		}
	    return instance;
	}
	
	public int supportedSBLNumber = 10;
	
	public enum DBType {CASSANDRA, MYSQL, NONE};
	// This variable is derived from the jvm system properties and os system envs.
	private DBType supportedDB = DBType.NONE;
	
	private String dbTypeSystemProperty;
	private String dbTypeSystemEnv;
	private String dbType;
	
	private String mySqlURLSystemProperty;
	private String mySqlURLSystemEnv;
	private String mySqlURL;

	private String cassandraNodeSystemProperty;
	private String cassandraNodeSystemEnv;
	private String cassandraNode;

	private String keySpaceNameSystemProperty;
	private String keySpaceNameSystemEnv;
	private String keySpaceName;

	private void decideDBType(){
		if(dbType != null) {
			if(dbType.equals("CASSANDRA")) {
				try {
					Class.forName("com.datastax.driver.core.Cluster");
				} catch(ClassNotFoundException e) {
					supportedDB = DBType.NONE;
					return;
				}
				if(cassandraNode == null || keySpaceName == null) supportedDB = DBType.NONE;
				else if(cassandraNode.trim().isEmpty() || keySpaceName.trim().isEmpty()) supportedDB = DBType.NONE;
				else supportedDB = DBType.CASSANDRA;
				return;
			}else if(dbType.equals("MYSQL")){
				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch(ClassNotFoundException e) {
					supportedDB = DBType.NONE;
					return;
				}
	            if(mySqlURL == null) supportedDB = DBType.NONE;
	            else if(mySqlURL.trim().isEmpty()) supportedDB = DBType.NONE;
	            else supportedDB = DBType.MYSQL;
	            return;
			}else {
				supportedDB = DBType.NONE;
				return;
			}
		} else {
			supportedDB = DBType.NONE;
			return;
		}
	}
	
	public DBType dbType() {
		return supportedDB;
	}
	
	public String getMySqlURL(){
		return mySqlURL;
	}

	public String getCassandraNode(){
		return cassandraNode;
	}
	public String getKeySpaceName(){
		return keySpaceName;
	}
}
