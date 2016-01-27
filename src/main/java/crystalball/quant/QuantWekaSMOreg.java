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
 * QuantWekaSMOreg.java
 * Copyright (C) 2016 Hui Ma
 * 
 */

package crystalball.quant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.WekaForecaster;
import crystalball.backtest.PositionState;
import crystalball.quant.QuantDecision.ActionSuggestion;

/**
 * Helper class that executes CrystalBall schemes from the command line. 
 * Takes stock data source entered by the user - e.g.<br>
 * <br>
 * 
 * java crystalball.Run ib <br>
 * <br>
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */
import crystalball.util.BarData;

/**
 * Give trade suggestion based on SVM prediction.
 * 
 * @author Hui Ma (huima58{[at]}gmail{[dot]}com)
 * @version $Revision: 1 $
 * 
 */

public class QuantWekaSMOreg extends Quant {
	private Instances instances = null;
	private Attribute id = new Attribute("id");
	private Attribute close = new Attribute("close");
	
	private static final int predictSteps = 6;
	private double predicted[] = new double[predictSteps];
	
	private static final int dataSetLength = 156 + 78;
	private WekaForecaster forecaster;
	
	public QuantWekaSMOreg(){
		super();
	}
	
  	public void forecast(BarData barData) {
  		forecast(barData, 0, barData.dataLen - 1);
  	}
  	
  	public void forecast(BarData barData, int startBarId, int endBarId) {
        makeDataSet(barData, startBarId, endBarId);
        try {
            if(isLastBarOfHalfHour(barData, endBarId) || forecaster == null) {
                // new forecaster
                forecaster = new WekaForecaster();
    
                // set the targets we want to forecast. This method calls
                // setFieldsToLag() on the lag maker object for us
                forecaster.setFieldsToForecast("close");
    
                // set the underlying classifier as SMOreg (SVM)
                forecaster.setBaseForecaster(new SMOreg());
    
                forecaster.getTSLagMaker().setTimeStampField("id"); // date time stamp
                forecaster.getTSLagMaker().setMinLag(1);
                forecaster.getTSLagMaker().setMaxLag(78);
    
                // add a month of the year indicator field
                forecaster.getTSLagMaker().setAddMonthOfYear(false);
    
                // add a quarter of the year indicator field
                forecaster.getTSLagMaker().setAddQuarterOfYear(false);
    
                // build the model
                forecaster.buildForecaster(instances);
            }

            // prime the forecaster with enough recent historical data
            // to cover up to the maximum lag. 
            forecaster.primeForecaster(instances);

            // forecast for 6 units beyond the end of the training data
            List<List<NumericPrediction>> forecast = forecaster.forecast(predictSteps, System.out);

            // output the predictions. Outer list is over the steps; inner list is over
            // the targets
            for (int i = 0; i < predictSteps; i++) {
                List<NumericPrediction> predsAtStep = forecast.get(i);
                for (int j = 0; j < 1; j++) {
                    NumericPrediction predForTarget = predsAtStep.get(j);
                    predicted[i] = predForTarget.predicted();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
  	}
  	
  	private void makeDataSet(BarData barData, int startBarId, int endBarId){
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		attrs.add(id);
		attrs.add(close);
		instances = new Instances("instanceName", attrs, endBarId - startBarId + 1); 
		
  		for(int i=startBarId; i<=endBarId; i++) {
	  		// Create empty instance with three attribute values 
	  		Instance inst = new DenseInstance(2); 
	
	  		inst.setValue(instances.attribute("id"), i); 
	  		inst.setValue(instances.attribute("close"), barData.close[i]); 
	  		inst.setDataset(instances);
	  		instances.add(inst);
  		}
  	}

	@Override
	public QuantDecision makeDecision(BarData barData, int id, PositionState positionState) {
		if(id < dataSetLength) return null;
		forecast(barData, id - dataSetLength, id);
		boolean isLong = barData.close[id] < predicted[0];
		int i = 1;
		for(; i<predictSteps; i++) {
			if(isLong) {
				if(barData.close[id] > predicted[i]) break;
			} else {
				if(barData.close[id] < predicted[i]) break;
			}
		}
		QuantDecision decision = new QuantDecision();
		if(i < predictSteps / 2) {
			decision.suggestion = ActionSuggestion.Neutral;
		}else if(i < predictSteps){
			if(isLong) decision.suggestion = ActionSuggestion.HoldLong;
			else decision.suggestion = ActionSuggestion.HoldShort;
		}else {
			if(isLong) decision.suggestion = ActionSuggestion.Long;
			else decision.suggestion = ActionSuggestion.Short;
		}
		return decision;
	}

    private boolean isLastBarOfHalfHour(BarData barData, int id){
        if(id == 0) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(barData.time[id]);
        int minute = cal.get(Calendar.MINUTE);
        return (minute == 25 || minute == 55);
    }
}