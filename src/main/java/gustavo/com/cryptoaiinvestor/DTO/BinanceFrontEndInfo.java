package gustavo.com.cryptoaiinvestor.DTO;

import java.util.List;

public class BinanceFrontEndInfo {
    private double balance;
    private double unrealizedProfit;

    private List<ActiveTrade> activeTrades;
    private List<OpenOrder> openOrders;

    public BinanceFrontEndInfo() {
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<OpenOrder> getOpenOrders() {
        return openOrders;
    }

    public void setOpenOrders(List<OpenOrder> openOrders) {
        this.openOrders = openOrders;
    }

    public List<ActiveTrade> getActiveTrades() {
        return activeTrades;
    }

    public void setActiveTrades(List<ActiveTrade> activeTrades) {
        this.activeTrades = activeTrades;
    }

    public double getUnrealizedProfit() {
        return unrealizedProfit;
    }

    public void setUnrealizedProfit(double unrealizedProfit) {
        this.unrealizedProfit = unrealizedProfit;
    }
}
