// ******************************************************************************************
// * MACD Strategy Dukascopy jforex app store https://www.dukascopy.com/jstore/ MACD Signal *
// * Adapted Alex Fuchs, 14.04.2015 added EMA 50 Filter "buy and sell only with the trend   *
// * Adapted sl 50 tp 50                                                                    *
// * use template 10mins with sma12 and sma26, rest will be drawn                           *
// ******************************************************************************************


package jforex.strategies.indicators;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.*;

import com.dukascopy.api.*;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.IIndicators.MaType;
import com.dukascopy.api.Filter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import com.dukascopy.api.*;

public class madax implements IStrategy {

    private IEngine engine;
    private IConsole console;
    private IHistory history;
    private IIndicators indicators;
    private IUserInterface userInterface;
    private int counter = 0;
    //@Configurable("maxPosAllowed")
    public int maxPosAllowed = 1;
    private IOrder order;
    //@Configurable("Applied price")
    public Filter filter = Filter.ALL_FLATS;
    @Configurable("Instrument")
    public Instrument instrument = Instrument.GBPAUD;
    @Configurable("Period")
    public Period selectedPeriod = Period.DAILY; //TEN.MINS Period.ONE_HOUR
    //@Configurable("Offer side")
    public OfferSide offerSide = OfferSide.BID;
    //@Configurable("Slippage")
    public double slippage = 3;
    @Configurable("Amount")
    public double amount = 0.001;
    @Configurable("Take profit pips")
    public int takeProfitPips = 200; //45
    @Configurable("Stop loss in pips")
    public int stopLossPips = 350; //40
    //@Configurable("Applied price")
    public AppliedPrice appliedPrice = AppliedPrice.CLOSE;
    //@Configurable("Fast period")
    public int fastMACDPeriod = 50;
    //@Configurable("Slow period")
    public int slowMACDPeriod = 100;
    //@Configurable("Signal period")
    public int signalMACDPeriod = 75;
    //@Configurable("Long MA type")
    public MaType longMAType = MaType.EMA;
    //@Configurable("Long MA time period")
    public int longMAPeriod1 = 1;
    //@Configurable("Long MA time period")
    public int longMAPeriod2 = 3;
    @Configurable("Long MA time period")
    public int longMAPeriod3 = 8;
    @Configurable("Long MA time period")
    public int longMAPeriod4 = 35;
    //@Configurable("Long MA time period")
    public int longMAPeriod5 = 5;
    //@Configurable("Long MA time period")
    public int longMAPeriod6 = 7;
    //@Configurable("Long MA time period")
    public int longMAPeriod7 = 22;
    //@Configurable("Long MA time period")
    public int longMAPeriod8 = 23;
    //@Configurable("Long MA time period")
    public int longMAPeriod9 = 24;
    //@Configurable("Super Long MA time period")
    public int longMAPeriod10 = 50;
    //@Configurable("ATR period")
    public int atrPeriod = 6;
    //@Configurable("DX+ period")
    //public int plusDiPeriod = 14;
    //@Configurable("DX- period")
    //public int minusDiPeriod = 14;
    //@Configurable("+/- points")
    //public int minusDiPeriod = 14;
    @Configurable("ADX period")
    public int adxPeriod = 10;
    //@Configurable("adx limit")
    public int limit = 25;
    
    //@Configurable("startTime")
    public int startTime = 9; //14
    //@Configurable("endTime")
    //public int endTime = startTime + 1;
    public int endTime = 11; //endTime = 12;
    //@Configurable("startTimeMinute")
    public int startTimeMinute = 20;
    //@Configurable("endTimeMinute")
    //public int endTimeMinute = startTimeMinute + 10;
    public int endTimeMinute = 21;
    //@Configurable("startTimeDay")
    public int startTimeDay = Calendar.MONDAY; 
    //@Configurable("endTimeDay")
    public int endTimeDay = Calendar.THURSDAY;
    //@Configurable("closingTimePositions")
    public int closingTimePositions = 25;
    //@Configurable(value = "Instrument")
    //public Instrument pInstrument = Instrument.EURUSD;
    private Instrument pInstrument = instrument;
    //@Configurable(value = "Trailing Stop, pips", stepSize = 0.1)
    public double tStop = 55555.0;
    //@Configurable(value = "Trailing Step, pips", stepSize = 0.1)
    public double tStep = 2.0;
    //@Configurable(value = "Position Label")
    public String positionLabel = "";
    private void printMe(Object toPrint) throws JFException {
        console.getOut().println(pInstrument.name() + "|| " + toPrint.toString());
    }   
    
    private double point() throws JFException {
        return pInstrument.getPipValue();
    }   
    
    private void trailPosition(IOrder order, ITick tick) throws JFException {
        if (order != null && order.getState().equals(IOrder.State.FILLED)) {
            if (order.isLong()) {
                double newSL = tick.getBid() - tStop*point();
                if (tick.getBid() > order.getOpenPrice() + tStop*point() && newSL >= order.getStopLossPrice() + tStep*point()) {
                    printMe("Trailing Stop for LONG position: " + order.getLabel() + "; open price = " + order.getOpenPrice() + "; old SL = " + order.getStopLossPrice() + "; new SL = " + Double.toString(tick.getBid() - tStop*point()));
                    order.setStopLossPrice(newSL);
                    order.waitForUpdate(2000);
                }
            } else {
                double newSL = tick.getAsk() + tStop*point();
                if (tick.getAsk() < order.getOpenPrice() - tStop*point() && (newSL <= order.getStopLossPrice() - tStep*point() || order.getStopLossPrice() == 0.0)) {
                    printMe("Trailing Stop for SHORT position: " + order.getLabel() + "; open price = " + order.getOpenPrice() + "; old SL = " + order.getStopLossPrice() + "; new SL = " + Double.toString(tick.getAsk() + tStop*point()));
                    order.setStopLossPrice(newSL);
                    order.waitForUpdate(2000);
                }
            }
        }
    }   


    @SuppressWarnings("serial")
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") {

        {
            setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    };
    private String strategyName = this.getClass().getSimpleName(); 
    //private void printMe(Object toPrint) throws JFException {
    //console.getOut().println(pInstrument.name() + "|| " + toPrint.toString());
    //}
    
    @Override
    public void onStart(IContext context) throws JFException {
        this.console = context.getConsole();
        this.indicators = context.getIndicators();
        this.history = context.getHistory();
        this.engine = context.getEngine();
        //this.context = context;
        this.userInterface = context.getUserInterface();
        
        Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(pInstrument);
        context.setSubscribedInstruments(instruments, true);
    
        printMe("Strategy " + strategyName + " is started");
        if (positionLabel.length() == 0) {
            //printMe("Position Label parameter not defined. Trailing stop for all positions");
        }

        IChart chart = context.getChart(instrument);
        if (chart != null) {
            //chart.addIndicator(indicators.getIndicator("MACD"), new Object[]{fastMACDPeriod, slowMACDPeriod, signalMACDPeriod});
            //chart.addIndicator(indicators.getIndicator("plusDI"), new Object[]{adxPeriod});
            //chart.addIndicator(indicators.getIndicator("minusDI"), new Object[]{adxPeriod});
            //chart.addIndicator(indicators.getIndicator("ATR"), new Object[]{atrPeriod});
            //chart.addIndicator(indicators.getIndicator("ADX"), new Object[]{adxPeriod});
            //chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod1, longMAType.ordinal()});
            //chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod2, longMAType.ordinal()});
            //chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod3, longMAType.ordinal()});
            chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod4, longMAType.ordinal()});
            //chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod5, longMAType.ordinal()});
            //chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod6, longMAType.ordinal()});
            //chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod7, longMAType.ordinal()});
            //chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod8, longMAType.ordinal()});
            //chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod9, longMAType.ordinal()});
            //chart.addIndicator(indicators.getIndicator("MA"), new Object[]{longMAPeriod10, longMAType.ordinal()});
        }
    }
    public static final int HIST = 2;
    
    
    

    

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (period != this.selectedPeriod || instrument != this.instrument) {
            return;
        }
        
        long barStart = history.getBarStart(selectedPeriod, askBar.getTime());
        
        boolean canTrade = tradingHours(askBar.getTime(), startTime, endTime); //trading hours rule
        //boolean canTrade2 = tradingMinutes(askBar.getTime(), startTimeMinute, endTimeMinute); //trading minutes rule
        boolean canTrade3 = tradingDays(askBar.getTime(), startTimeDay, endTimeDay); //trading days rule
        
 
        //if ((!canTrade) && (!canTrade3)) return;
        //if (!canTrade3) return;
        //if (!canTrade2) return;
        //if (!canTrade) return;
        
        
        if (runningPos(instrument)>=maxPosAllowed) return;
        
        //printMe("now waiting 15 seconds...");
        //try { Thread.sleep(15000);
        //} catch (InterruptedException ex) {
        //}
        
        //double[] atr = indicators.atr(instrument, selectedPeriod, offerSide, atrPeriod, filter, 1000, barStart, 0);
        //double[] plusDi = indicators.plusDi(instrument, selectedPeriod, offerSide, plusDiPeriod, filter, 1000, barStart, 0);
        //double[] minusDi = indicators.minusDi(instrument, selectedPeriod, offerSide, minusDiPeriod, filter, 1000, barStart, 0);
        double[] adx = indicators.adx(instrument, selectedPeriod, offerSide, adxPeriod, filter, 1000, barStart, 0);
        
        

        int PREV = 998;
        int NEW = 999;
        
        //double[] macd0 = indicators.macd(instrument, this.selectedPeriod, offerSide, appliedPrice, fastMACDPeriod, slowMACDPeriod, signalMACDPeriod, 0);
        //double[] macd1 = indicators.macd(instrument, this.selectedPeriod, offerSide, appliedPrice, fastMACDPeriod, slowMACDPeriod, signalMACDPeriod, 1);
        double[] longMA1 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod1, longMAType, filter, 2, askBar.getTime(), 0);
        double[] longMA2 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod2, longMAType, filter, 2, askBar.getTime(), 0);
        double[] longMA3 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod3, longMAType, filter, 2, askBar.getTime(), 0);
        double[] longMA4 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod4, longMAType, filter, 2, askBar.getTime(), 0);
        //double[] longMA5 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod5, longMAType, filter, 2, askBar.getTime(), 0);
        //double[] longMA6 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod6, longMAType, filter, 2, askBar.getTime(), 0);
        //double[] longMA7 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod7, longMAType, filter, 2, askBar.getTime(), 0);
        //double[] longMA8 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod8, longMAType, filter, 2, askBar.getTime(), 0);
        //double[] longMA9 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod9, longMAType, filter, 2, askBar.getTime(), 0);
        //double[] longMA10 = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod10, longMAType, filter, 2, askBar.getTime(), 0);
        //double[] longMA = indicators.ma(instrument, this.selectedPeriod, offerSide, appliedPrice, longMAPeriod, longMAType, filter, 2, askBar.getTime(), 0);
        int CURRENT = 1;
        int PREVIOUS = 0;
        //double diff_fall = 0; double diff_rais = 0; int x_fall = 0; int x_rais = 0;
        //diff_fall = (longMA2[PREVIOUS] - longMA2[CURRENT]) * 10000; x_fall = (int)Math.round(diff_fall);
        //diff_rais = (longMA2[CURRENT] - longMA2[PREVIOUS]) * 10000; x_rais = (int)Math.round(diff_rais);
        
        
       //if (macd0[HIST] > 0 && macd1[HIST] <= 0 && longMA[PREVIOUS] < longMA[CURRENT]){
            //closeOrder(order);
            //printMe("open SELL  " + macd0[HIST] + " " + macd1[HIST] + "  may the force be with you ");
            //order = submitOrder(OrderCommand.SELL);
            printMe("");
        //printMe("-----------begin check for sell------------------------------------------------");    
        //if (longMA1[CURRENT] > longMA2[CURRENT] && longMA2[CURRENT] > longMA3[CURRENT]){
        if (longMA1[CURRENT] > longMA2[CURRENT]){
         } else if (longMA2[CURRENT] > longMA3[CURRENT]) {
            //printMe("raising condition2 ... ");
        } else if (longMA3[CURRENT] > longMA4[CURRENT]) {
            //printMe("getting out...");
            //closeAllPositions();
            
         //} else if (longMA2[CURRENT] > longMA3[CURRENT]) {
            //printMe("getting out...");
                       
        //} else if (longMA3[CURRENT] > longMA4[CURRENT]) {
            //printMe("test ");
        //} else if (longMA4[CURRENT] > longMA5[CURRENT]) {
            //printMe("test ");
        //} else if (longMA6[CURRENT] > longMA7[CURRENT]) {
            //printMe("test ");
        //} else if (longMA7[CURRENT] > longMA8[CURRENT]) {
            //printMe("test ");
        //} else if (longMA8[CURRENT] > longMA9[CURRENT]) {
            //printMe("test ");
        //} else if (longMA5[CURRENT] > longMA10[CURRENT]) {
            //printMe("super LongMA " + longMA10[CURRENT] + "smaller than " + longMA10[PREVIOUS] + "falling -> only sell possible... ");
        //} else if (adx[PREV] > limit) {
            //printMe("adx > " + limit +  " open sell position... ");           
        } else {
           //printMe (" open Sell ");
           //closeOrder(order);
           //printMe("open SELL  may the force be with you!  " + "longMA2[CURRENT] " + longMA2[CURRENT] + " is smaller than " + "longMA2[PREVIOUS] " + longMA2[PREVIOUS] );
           //double diff;
           //diff = (longMA2[CURRENT] - longMA2[PREVIOUS]) * 10000
           //int x = (int)Math.round(diff_fall);
           //printMe("diff falling is: " + x_fall ); // calculate diff von longMA2 and format to nice value
           //printMe("getting out...");
           //closeAllPositions();
           order = submitOrder(OrderCommand.SELL);
           //order = submitOrder(OrderCommand.BUY);
        }

             
          
        //if (macd0[HIST] > 0 && macd1[HIST] <= 0 && longMA[PREVIOUS] < longMA[CURRENT]){
            //closeOrder(order);
            //printMe("open BUY  " + macd0[HIST] + " " + macd1[HIST] + "  may the force be with you ");
            //order = submitOrder(OrderCommand.BUY);
            printMe("");
        //printMe("-----------begin check for buy------------------------------------------------");
        if (longMA1[CURRENT] < longMA2[CURRENT]){
         } else if (longMA2[CURRENT] < longMA3[CURRENT]) {
            //printMe("raising condition2 ... ");
        } else if (longMA3[CURRENT] < longMA4[CURRENT]) {
            //printMe("test ");
        //} else if (longMA4[CURRENT] < longMA5[CURRENT]) {
            //printMe("test ");
        //} else if (longMA6[CURRENT] < longMA7[CURRENT]) {
            //printMe("test ");
        //} else if (longMA7[CURRENT] < longMA8[CURRENT]) {
            //printMe("test ");
        //} else if (longMA8[CURRENT] < longMA9[CURRENT]) {
            //printMe("test ");
        //} else if (longMA5[CURRENT] < longMA10[CURRENT]) {
            // printMe("super LongMA " + longMA10[CURRENT] + "larger than " + longMA10[PREVIOUS] + "raising -> only buy possible... ");
        //} else if (adx[PREV] > limit) {
            //printMe("adx > " + limit + " open buy position... ");
        } else {
            //printMe("erfülltBuy ");
           //closeOrder(order);
           //printMe("open BUY  may the force be with you! "+ " longMA2[CURRENT] " + longMA2[CURRENT] + " is greater than " + " longMA2[PREVIOUS] " + longMA2[PREVIOUS] );
           printMe("open BUY may the force be with you!");
           //double diff;
           //diff = (longMA2[CURRENT] - longMA2[PREVIOUS]) * 10000;
           //int x = (int)Math.round(diff_rais);
           //printMe("diff raising is: " + x_rais );// calculate diff von longMA2 and format to nice value
           order = submitOrder(OrderCommand.BUY);
           //order = submitOrder(OrderCommand.SELL);     
        }
   
        //if (longMA1[CURRENT] < longMA2[CURRENT] && longMA2[CURRENT] < longMA3[CURRENT] && longMA3[CURRENT] < longMA4[CURRENT] && longMA4[CURRENT] < longMA5[CURRENT] && longMA5[CURRENT] < longMA6[CURRENT] && longMA6[CURRENT] < longMA7[CURRENT] && longMA7[CURRENT] < longMA8[CURRENT] && longMA8[CURRENT] < longMA9[CURRENT] && longMA9[CURRENT] < longMA10[CURRENT]){
        //if (macd0[HIST] < 0 && macd1[HIST] >= 0  && longMA[PREVIOUS] > longMA[CURRENT]) {
            //closeOrder(order);
            //printMe("open SELL, may the force be with you ");
            //order = submitOrder(OrderCommand.SELL);
        
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (instrument != this.instrument) {
            return;
        }
       if (positionLabel.length() == 0) {
            for (IOrder o : engine.getOrders(pInstrument)) {
                if (o.getState().equals(IOrder.State.FILLED)) {
                    //trailPosition(o, tick); alex uncommented wegen falschem trailing
                }
            }
        } else {
            IOrder order = engine.getOrder(positionLabel);
            if (order != null && order.getState().equals(IOrder.State.FILLED)) {
                //trailPosition(order, tick); alex uncommented wegen falschem trailing
            }
        }

    }

    public void onMessage(IMessage message) throws JFException {
    }

    public void onAccount(IAccount account) throws JFException {
    }

    public void onStop() throws JFException {
        printMe("Strategy " + strategyName + " is stopped");
    }

    private IOrder submitOrder(OrderCommand orderCmd) throws JFException {

        double stopLossPrice, takeProfitPrice;

        // Calculating order price, stop loss and take profit prices
        if (orderCmd == OrderCommand.BUY) {
            stopLossPrice = history.getLastTick(this.instrument).getBid() - getPipPrice(this.stopLossPips);
            takeProfitPrice = history.getLastTick(this.instrument).getBid() + getPipPrice(this.takeProfitPips);
        } else {
            stopLossPrice = history.getLastTick(this.instrument).getAsk() + getPipPrice(this.stopLossPips);
            takeProfitPrice = history.getLastTick(this.instrument).getAsk() - getPipPrice(this.takeProfitPips);
        }

        return engine.submitOrder(getLabel(instrument), instrument, orderCmd, amount, 0, 20, stopLossPrice, takeProfitPrice);
        
    }
    
    private int runningPos(Instrument instrument) throws JFException {
        return engine.getOrders(instrument).size();}

    private void closeOrder(IOrder order) throws JFException {
        if (order == null) {
            return;
        }
        if (order.getState() != IOrder.State.CLOSED && order.getState() != IOrder.State.CREATED && order.getState() != IOrder.State.CANCELED) {
            order.close();
            order = null;
        }
    }

    private double getPipPrice(int pips) {
        return pips * this.instrument.getPipValue();
    }

    private String getLabel(Instrument instrument) {
        String label = instrument.name();
        label = label + (counter++);
        label = label.toUpperCase();
        return label;
    }

    private void print(Object... o) {
        for (Object ob : o) {
            //console.getOut().print(ob + "  ");
            if (ob instanceof double[]) {
                print((double[]) ob);
            } else if (ob instanceof double[]) {
                print((double[][]) ob);
            } else if (ob instanceof Long) {
                print(dateToStr((Long) ob));
            } else {
                print(ob);
            }
            print(" ");
        }
        console.getOut().println();
    }

    private void print(Object o) {
        console.getOut().print(o);
    }

    private void println(Object o) {
        console.getOut().println(o);
    }

    private void print(double[] arr) {
        println(arrayToString(arr));
    }

    private void print(double[][] arr) {
        println(arrayToString(arr));
    }

    private void closeAllPositions() throws JFException {
        List<IOrder> openOrders = engine.getOrders();
        if (openOrders.isEmpty()) return;
        printMe("openOrders: " + openOrders);
        printMe("closing all of them... " + openOrders );
        for (IOrder order : openOrders) order.close(); //close order engine
    }    

    private void printIndicatorInfos(IIndicator ind) {
        for (int i = 0; i < ind.getIndicatorInfo().getNumberOfInputs(); i++) {
            println(ind.getIndicatorInfo().getName() + " Input " + ind.getInputParameterInfo(i).getName() + " " + ind.getInputParameterInfo(i).getType());
        }
        for (int i = 0; i < ind.getIndicatorInfo().getNumberOfOptionalInputs(); i++) {
            println(ind.getIndicatorInfo().getName() + " Opt Input " + ind.getOptInputParameterInfo(i).getName() + " " + ind.getOptInputParameterInfo(i).getType());
        }
        for (int i = 0; i < ind.getIndicatorInfo().getNumberOfOutputs(); i++) {
            println(ind.getIndicatorInfo().getName() + " Output " + ind.getOutputParameterInfo(i).getName() + " " + ind.getOutputParameterInfo(i).getType());
        }
        console.getOut().println();
    }

    public static String arrayToString(double[] arr) {
        String str = "";
        for (int r = 0; r < arr.length; r++) {
            str += "[" + r + "] " + (new DecimalFormat("#.#######")).format(arr[r]) + "; ";
        }
        return str;
    }

    public static String arrayToString(double[][] arr) {
        String str = "";
        if (arr == null) {
            return "null";
        }
        for (int r = 0; r < arr.length; r++) {
            for (int c = 0; c < arr[r].length; c++) {
                str += "[" + r + "][" + c + "] " + (new DecimalFormat("#.#######")).format(arr[r][c]);
            }
            str += "; ";
        }
        return str;
    }

    public String toDecimalToStr(double d) {
        return (new DecimalFormat("#.#######")).format(d);
    }

    public String dateToStr(Long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") {

            {
                setTimeZone(TimeZone.getTimeZone("GMT"));
            }
        };
        return sdf.format(time);
    }
    //defining trading hours
    private boolean tradingHours(long barTime, int bHour, int eHour) throws JFException{
        boolean tradingTime = false;
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(barTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        //printMe(" hour " + hour );
        
        if (hour >= bHour && hour < eHour) tradingTime = true;
        //if (!tradingTime) closeAllPositions();
        //if (hour >= eHour + 3) closeAllPositions();
        if (hour >= closingTimePositions) closeAllPositions();
        return tradingTime;
    }
    //defining trading minutes
    private boolean tradingMinutes(long barTime, int bMinutes, int eMinutes) throws JFException{
        boolean tradingTimeMinutes = false;
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(barTime);
        int minute = cal.get(Calendar.MINUTE);
        //printMe(" hour " + hour );
        
        if (minute >= bMinutes && minute < eMinutes) tradingTimeMinutes = true;
        //if (!tradingTime) closeAllPositions();
        return tradingTimeMinutes;
    }
    //defining trading days
    private boolean tradingDays(long barTime, int bDays, int eDays) throws JFException{
        boolean tradingTimeDays = false;
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(barTime);
        //int day = cal.get(Calendar.DAY_OF_WEEK);
        //printMe(" hour " + hour );
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        //boolean tradingTimeDays = ((dow >= Calendar.MONDAY) && (dow <= Calendar.FRIDAY));
        if (dow >= bDays && dow < eDays) tradingTimeDays = true;
        //if (!tradingTime) closeAllPositions();
        return tradingTimeDays;
    }
}
