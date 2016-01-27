CrystalBall is a data mining research platform that helps developing strategies to forecast financial time series using classification techniques. The platform implements the tools to acquire financial historical data, forecaste price, backtest strategy, and monitor the real-time market. User can focus on devloping classification methods with all these tools ready. 

CrystallBall acquires 5 minutes bar data through InteractiveBrokers TWS API and forcasts prices through Weka machine learning software tool.

Project Installation:
Install Pentaho time series analysis and forecasting package to local maven repository:
mvn install:install-file -Dfile=lib\pdm-timeseriesforecasting-ce-TRUNK-SNAPSHOT.jar -DgroupId=com.pentaho -DartifactId=pdm-timeseriesforecasting-ce -Dversion=1.0.16 -Dpackaging=jar

Install TwsApi to local maven repository:
mvn install:install-file -Dfile=lib\TwsApi.jar -DgroupId=com.interactivebrokers.tws -DartifactId=TwsApi -Dversion=9.72.08 -Dpackaging=jar

Data Analysis Tools:
Weka machine learning algorithms
http://www.cs.waikato.ac.nz/~ml/

Pentaho time series analysis and forcasting plugin
http://weka.sourceforge.net/packageMetaData/timeseriesForecasting/Latest.html

Databases:
MySQL
www.mysql.com

Cassandra
cassandra.apache.org

Market Data Source:
IB API
www.interactivebrokers.com/

Java version:
1.8

Command Example:
java -DDB_TYPE=CASSANDRA -DCASSANDRA_NODE=nodeIP -DKEYSPACE_NAME=demo -jar crystalBall-1.0.0.jar backtest SPY 2015-01-02 2015-12-31

java -DDB_TYPE=MYSQL -DMYSQL_URL="jdbc:mysql://mysql_server/db_name?user=xxx&password=yyy" -jar crystalBall-1.0.0.ja
r backtest SPY 2015-01-02 2015-12-31



