package DB_Project;

public class Address {
    private String country;
    private String city;
    private String street;
    private int houseNumber;

    public Address(){

    }
    public Address(String country,String city, String street, int houseNumber){
        this.country = country;
        this.city = city;
        this.street = street;
        this.houseNumber = houseNumber;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

public String getCountry() {
    return country;
}

    @Override
    public String toString(){
        return "Address: " + "\n" +
                "country='" + country + '\'' + "\n" +
                "city='" + city + '\'' + "\n" +
                "street='" + street +'\'' + "\n" +
                "houseNumber='" + houseNumber + '\'' + "\n";
    }
}