import java.time.LocalDate;
import java.util.Arrays;

public class CartHistory{
    private Product[] products = new Product[0];
    private LocalDate time = LocalDate.now();
    
    public CartHistory() {
    }
    public CartHistory(Product[] products) {
        this.products = products;
    }

    public Product[] getProducts() {
        return products;
    }

    public LocalDate getTime() {
        return time;
    }

    public void setTime(LocalDate time) {
        this.time = time;
    }

    @Override
    public String toString(){
        return "date(" + time + ")" +" "+
                Arrays.toString(products);
    }

}