package DB_Project;
import java.util.Arrays;

public class Seller extends Username implements Comparable<Seller> {

    public Seller() {
    }
    
    public Seller(String name, String passWord) {
        super(name, passWord);
    }

    // בנאי שמתאים לשליפה ממסד הנתונים
    public Seller(int userId, String name, String passWord) {
        super(userId, name, passWord);
    }

    @Override
    public String toString() {
        return  "seller Info:\n" +
                "ID='" + super.getUserId() + '\'' + "\n" + // הוספתי הדפסה של ה-ID כדי שיהיה לך נוח
                "name='" + getName() + '\'' + "\n" +
                "passWord='" + getPassWord() + '\'' + "\n" +
                "products=" + Arrays.toString(Arrays.copyOfRange(getProducts(), 0, getLogicSizeProduct()));
    }

    @Override
    public int compareTo(Seller other) {
        return super.getLogicSizeProduct() - other.getLogicSizeProduct();
    }
}