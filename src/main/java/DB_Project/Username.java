package DB_Project;

import java.util.Arrays;

public class Username {
    private String name;
    private String passWord;
    private Product[] products = new Product[0];
    private int logicSizeProduct=0;

    public Username(){

    }

    public Username(String name, String passWord){
        this.name = name;
        this.passWord = passWord;
    }

    public String getName() {
        return name;
    }

    public String getPassWord() {
        return passWord;
    }

    public Product[] getProducts() {
        return products;
    }


    public int getLogicSizeProduct() {
        return logicSizeProduct;
    }

    public boolean setName(String name) {
        this.name = name;
        return true;
    }

    public boolean setPassWord(String passWord) {
        this.passWord = passWord;
        return true;
    }

    public void setLogicSizeProduct(int logicSizeProduct) {
        this.logicSizeProduct = logicSizeProduct;
    }

    public boolean setProducts(Product[] product) {
        this.products = Arrays.copyOf(product,product.length);
        return true;
    }

    void addProduct(Product product){
        if(logicSizeProduct == 0) {
            products = Arrays.copyOf(products, products.length + 1);
            products[0] = product;
        }
        else{
            if(logicSizeProduct < products.length){
                products[logicSizeProduct] = product;
            }
            else {
                products = Arrays.copyOf(products, products.length * 2);
                products[logicSizeProduct] = product;
            }
        }
        logicSizeProduct++;
    }



}