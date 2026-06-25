import java.util.Arrays;

public class Seller extends Username implements Comparable<Seller>{

    public Seller() {
    }
    public Seller(String name, String passWord) {
        super(name, passWord);
    }

    @Override
    public String toString(){
        return  "seller Info:\n"+
                "name='" + getName() + '\'' + "\n" +
                "passWord='" + getPassWord() + '\''+"\n"+
                "products=" + Arrays.toString(Arrays.copyOfRange(getProducts(), 0, getLogicSizeProduct()));
    }

    @Override
    public int compareTo(Seller other) {
        return super.getLogicSizeProduct() - other.getLogicSizeProduct();
    }
}