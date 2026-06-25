import java.util.Arrays;

public class SingleSourceOfTruth{
    private Buyer[] buyers = new Buyer[0];
    private Seller[] sellers = new Seller[0];
    private int logicSizeBuyers=0;
    private int logicSizeSellers=0;
    private float sum=0;

    public SingleSourceOfTruth() {
    }
    public SingleSourceOfTruth(Buyer[] buyers, Seller[] sellers) {
        this.buyers = buyers;
        this.sellers = sellers;
    }

    public Buyer[] getBuyers() {
        return buyers;
    }

    public Seller[] getSellers() {
        return sellers;
    }

    public int getLogicSizeSellers() {
        return logicSizeSellers;
    }

    public int getLogicSizeBuyers() {
        return logicSizeBuyers;
    }

    public float getSum() {
        return sum;
    }

    public boolean setBuyers(Buyer[] buyers) {
        this.buyers = buyers;
        return true;
    }

    public boolean setSellers(Seller[] sellers) {
        this.sellers = sellers;
        return true;
    }

    void addBuyer(Buyer buyer){
        if(logicSizeBuyers == 0) {
            buyers = Arrays.copyOf(buyers, buyers.length + 1);
            buyers[0] = buyer;
        }
        else{
            if(logicSizeBuyers < buyers.length){
                buyers[logicSizeBuyers] = buyer;
            }
            else {
                buyers = Arrays.copyOf(buyers, buyers.length * 2);
                buyers[logicSizeBuyers] = buyer;
            }
        }
        logicSizeBuyers++;
    }

    void addSeller(Seller seller){
        if(logicSizeSellers == 0) {
            sellers = Arrays.copyOf(sellers, sellers.length + 1);
            sellers[0] = seller;
        }
        else{
            if(logicSizeSellers < sellers.length){
                sellers[logicSizeSellers] = seller;
            }
            else {
                sellers = Arrays.copyOf(sellers, sellers.length * 2);
                sellers[logicSizeSellers] = seller;
            }
        }
        logicSizeSellers++;
    }

    boolean isBuyerExist(String name){
        for(int i = 0; i < logicSizeBuyers; i++){
            if(buyers[i]==null) {
                return false;
            }
            if(buyers[i].getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    boolean isSellerExist(String name){
        for(int i = 0; i < logicSizeSellers; i++){
            if(sellers[i]==null){
                return false;
            }
            if(sellers[i].getName().equals(name)){
                return true;
            }
        }
        return false;
    }

}