package gustavo.com.cryptoaiinvestor.DTO;

public class ActiveTrade {

    private String symbol;
    private double entryPrice;
    private double positionAmt;
    private double stopLoss;
    private double takeProfit;
    private double unrealizedProfit;
    private double leverage;
    private String positionSide;

    public ActiveTrade() {
    }

    public double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public double getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(double takeProfit) {
        this.takeProfit = takeProfit;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public double getPositionAmt() {
        return positionAmt;
    }

    public void setPositionAmt(double positionAmt) {
        this.positionAmt = positionAmt;
    }

    public double getLeverage() {
        return leverage;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }

    public String getPositionSide() {
        return positionSide;
    }

    public void setPositionSide(String positionSide) {
        this.positionSide = positionSide;
    }

    public double getUnrealizedProfit() {
        return unrealizedProfit;
    }

    public void setUnrealizedProfit(double unrealizedProfit) {
        this.unrealizedProfit = unrealizedProfit;
    }
}
