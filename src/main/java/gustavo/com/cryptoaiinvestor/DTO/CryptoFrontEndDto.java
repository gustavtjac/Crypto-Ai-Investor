package gustavo.com.cryptoaiinvestor.DTO;

public class CryptoFrontEndDto {

    private String ticker;
    private String img;
    private String name;

    public CryptoFrontEndDto(String ticker, String name, String img) {
        this.ticker = ticker;
        this.name = name;
        this.img = img;
    }

    public CryptoFrontEndDto() {
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
