public class SpecialPackProd extends Product {
    private float extraPay;

    public SpecialPackProd() {
    }
    public SpecialPackProd(String name, float price, Category category,boolean isSpecialProd, float extraPay) {
        super(name, price, category, isSpecialProd);
        this.extraPay = extraPay;
    }

    public float getExtraPay() {
        return extraPay;
    }

    public void setExtraPay(float extraPay) {
        this.extraPay = extraPay;
    }

    @Override
    public String toString() {
        float newPrice = super.getPrice()+extraPay;
        if(super.getisSpecialProd()) {
            return "{name:'" + super.getName() + '\'' + "," +
                    "price:'" + newPrice + '\'' + "," +
                    "type':" + super.getCategory() + '\'' + "}";
        }
        else{
            return "{name:'" + super.getName() + '\'' + "," +
                    "price:'" + super.getPrice() + '\'' + "," +
                    "type':" + super.getCategory() + '\'' + "}";
        }
    }


}