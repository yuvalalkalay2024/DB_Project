package DB_Project;
public class Product implements Comparable<Product>, Cloneable {

    public enum Category {
        Children,
        Electricity,
        Office,
        Clothing
    }
    private float price;
    private String name;
    private int ID = 0;
    private Category category;
    private boolean isSpecialProd = false;

    public Product() {
    }
    public Product(String name, float price, Category category, boolean isSpecialProd) {
        this.price = price;
        this.name = name;
        this.category = category;
        this.isSpecialProd = isSpecialProd;
        this.ID++;
    }

    public Product(int id, String name, float price, Category category, boolean isSpecialProd) {
        this.price = price;
        this.name = name;
        this.category = category;
        this.isSpecialProd = isSpecialProd;
        this.ID = id;
    }

    public Product(int id, String name, float price, String category, boolean isSpecialProd) {
        this.price = price;
        this.name = name;
        switch(category){
            case("Children"):
                this.category = Category.Children;
                break;
            case("Electricity"):
                this.category = Category.Electricity;
                break;
            case("Office"):
                this.category = Category.Office;
                break;
            case("Clothing"):
                this.category = Category.Clothing;
                break;
        }
        this.isSpecialProd = isSpecialProd;
        this.ID = id;
    }

    public Product(Product other) {
        this.price = other.price;
        this.name = other.name;
        this.category = other.category;
    }

    public int getID() {
        return ID;
    }

    public float getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public boolean getisSpecialProd() {
        return isSpecialProd;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setSpecialProd(boolean specialProd) {
        isSpecialProd = specialProd;
    }

    @Override
    public String toString(){
        return "{name:'" + name + '\'' + "," +
                "price:'" + price + '\''+"," +
                "type':"+category+ '\''+"}";
    }

    @Override
    public Product clone() {
        try {
            Product clone = (Product) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int compareTo(Product prod2) {
        return this.ID - prod2.getID();
    }
}