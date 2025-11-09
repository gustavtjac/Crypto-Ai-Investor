package gustavo.com.cryptoaiinvestor.DTO;

public class OpenOrder  {
    private String symbol;
    private String orderId;
    private String side;
    private String type;
    private double price;
    private double origQty;
    private double executedQty;
    private String status;


    public OpenOrder() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOrigQty() {
        return origQty;
    }

    public void setOrigQty(double origQty) {
        this.origQty = origQty;
    }

    public double getExecutedQty() {
        return executedQty;
    }

    public void setExecutedQty(double executedQty) {
        this.executedQty = executedQty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
