package DB_Project;

import java.util.Arrays;

public class Buyer extends Username implements Comparable<Buyer>{
    private Address address = new Address();
    private CartHistory[] PaymentHistory = new CartHistory[0];
    private int logicSizePaymentHistory=0;

    public Buyer() {
    }
    public Buyer(String name, String passWord, Address address) {
        super(name, passWord);
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }


    public CartHistory[] getPaymentHistory() {
        return PaymentHistory;
    }

    public int getLogicSizePaymentHistory() {
        return logicSizePaymentHistory;
    }

    public boolean setAddress(Address address) {
        this.address = address;
        return true;
    }

    public boolean setPaymentHistory(CartHistory[] paymentHistory) {
        PaymentHistory = paymentHistory;
        return true;
    }

    float paymentPrice(){
        float sum = 0;
        for(int i = 0; i < getLogicSizeProduct(); i++){
            sum += getProducts()[i].getPrice();
        }
        return sum;
    }

    void addPaymentHistory(Product[] products){
        CartHistory history = new CartHistory(products);
        if(logicSizePaymentHistory == 0) {
            PaymentHistory = Arrays.copyOf(PaymentHistory, PaymentHistory.length + 1);
            PaymentHistory[0] = history;
        }
        else{
            if(logicSizePaymentHistory < PaymentHistory.length){
                PaymentHistory[logicSizePaymentHistory] = history;

            }
            else {
                PaymentHistory = Arrays.copyOf(PaymentHistory, PaymentHistory.length * 2);
                PaymentHistory[logicSizePaymentHistory] = history;
            }
        }
        logicSizePaymentHistory++;
    }

    @Override
    public String toString(){
        return  "Buyer name='" + getName() + '\'' + "\n" +
                "passWord='" + getPassWord() + '\'' + "\n" +
                "address='" + address +
                "Cart=" + Arrays.toString(Arrays.copyOfRange(getProducts(), 0, getLogicSizeProduct())) + "\n" +
                "PaymentHistory=" + Arrays.toString(Arrays.copyOfRange(PaymentHistory, 0, logicSizePaymentHistory));
    }

    @Override
    public int compareTo(Buyer other) {
        return this.getName().compareTo(other.getName());
    }
}