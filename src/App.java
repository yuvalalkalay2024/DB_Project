// name: Yuval Alkalay  id: 207962770
// name: Almog Dinur    id: 211627054
// we are both in pini shlomi's class
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class App extends GenericFunctions{
    // Flag to keep the main loop running
    static boolean isMainRunning = true;
    static SingleSourceOfTruth data = new SingleSourceOfTruth();
    static Scanner s = new Scanner(System.in);

    // Function to add a new buyer to the system
    static void addBuyer() {
        String name;
        System.out.print("Enter buyer name: ");
        name = s.nextLine();

        while (data.isBuyerExist(name)) { // Loop to ensure a unique buyer name is entered
            System.out.println("The name you chose exists in the system");
            System.out.println("Enter buyer name: ");
            name = s.nextLine();
        }
        System.out.print("create password: ");
        String passWord = s.nextLine();
        System.out.println("address");
        System.out.print("enter country: ");
        String country = s.nextLine();
        System.out.print("enter city: ");
        String city = s.nextLine();
        System.out.print("enter street: ");
        String street = s.nextLine();
        int houseNumber;
        while (true) {
            try {
                System.out.print("enter house number: ");
                houseNumber = s.nextInt();
                s.nextLine();
                if (houseNumber > 0) {
                    break;
                } else {
                    throw new Exception("Invalid input. The House number must be a positive number!");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                s.nextLine();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        Address a = new Address(country, city, street, houseNumber);
        Buyer b = new Buyer(name, passWord, a);
        data.addBuyer(b);
    }

    // Function to add a new seller to the system
    static void addSeller() {
        String name;
        System.out.print("Enter seller name: ");
        name = s.nextLine();
        while (data.isSellerExist(name)) { // Loop to ensure a unique buyer name is entered
            System.out.println("The name you chose exists in the system");
            System.out.println("Enter seller name: ");
            name = s.nextLine();
        }
        System.out.print("create password: ");
        String passWord = s.nextLine();
        Seller seller = new Seller(name, passWord);
        data.addSeller(seller);
    }

    // Function to add a product to a seller (implementation incomplete)
    static void addProductToSeller() {
        ;
        for (int i = 1; i <= data.getLogicSizeSellers(); i++) {
            System.out.println(i + ") " + data.getSellers()[i - 1].getName());
        }
        int sellerNumber = ExceptionCheckDomain("enter the seller's number: ",data.getLogicSizeSellers(), 1); // checks if input for the selected seller is valid
        System.out.print("Enter product name: ");
        String product = s.nextLine();
        float price = ExceptionCheckPositive("Enter product price: ");// checks if input for the product price valid
        int index = ExceptionCheckDomain("1) Children\n2) Electricity\n3) Office\n4) Clothing\n" +
                "enter the number of the category: ",4, 1);// checks if input for the selected product type is valid
        Product.Category[] categories = Product.Category.values();
        String answer = ExceptionCheckYesOrNO("do you wish to sell this product in a special packaging? yes/no: ");// checks that user inputs yes/no properly
        if (answer.equals("yes")) {
            float extraPay = ExceptionCheckPositive("what is your price for the special packaging? ");// checks if input for the price of the special packaging is valid
            SpecialPackProd newProduct = new SpecialPackProd(product, price, categories[index - 1],false, extraPay);
            data.getSellers()[sellerNumber - 1].addProduct(newProduct);
        } else {
            Product newProduct = new Product(product, price, categories[index - 1], false);
            data.getSellers()[sellerNumber - 1].addProduct(newProduct);
        }
    }

    // Function to add a product to a buyer (implementation incomplete)
    static void addProductToBuyer() {
        int counter = 0;
        Seller[] temp = new Seller[0];
        for (int i = 1; i <= data.getLogicSizeBuyers(); i++) {
            System.out.println(i + ") " + data.getBuyers()[i - 1].getName());
        }
        int buyerNumber = ExceptionCheckDomain("Enter buyer number: ",data.getLogicSizeBuyers(),1);// checks if input for the selected buyer is valid
        for (int i = 1; i <= data.getLogicSizeSellers(); i++) {//prints only the sellers that have products to sell
            if (data.getSellers()[i - 1].getLogicSizeProduct() != 0) {
                counter++;
                System.out.println(counter + ") " + data.getSellers()[i - 1].getName());
                temp = Arrays.copyOf(temp, counter);
                temp[counter - 1] = data.getSellers()[i - 1];
            }
        }
        if (counter == 0) {
            System.out.println("Error: there are no sellers who got initiated with products yet");
        } else {
            int sellerNumber = ExceptionCheckDomain("enter the seller's number: ", counter, 1);// checks if input for the selected seller is valid
            for (int i = 1; i <= temp[sellerNumber-1].getLogicSizeProduct(); i++) {
                System.out.println(i + ") " + temp[sellerNumber - 1].getProducts()[i - 1].getName() + " " + temp[sellerNumber - 1].getProducts()[i - 1].getPrice() + "$");
            }
            int productNumber = ExceptionCheckDomain("enter product number: "
                    ,temp[sellerNumber-1].getLogicSizeProduct(),1 );// checks if input for the selected product is valid
            if (temp[sellerNumber - 1].getProducts()[productNumber - 1] instanceof SpecialPackProd) {
                String answer = ExceptionCheckYesOrNO("the product: '" + temp[sellerNumber - 1].getProducts()[productNumber - 1].getName() +
                        "' can be packed in a special package,\n" + "would you like to pay " +
                        ((SpecialPackProd) temp[sellerNumber - 1].getProducts()[productNumber - 1]).getExtraPay() +
                        " for the special package?\ntype yes/no: ");// checks that user inputs yes/no properly
                Product p = data.getSellers()[sellerNumber - 1].getProducts()[productNumber - 1];
                if (answer.equals("yes")) {
                    float FinalPay = ((SpecialPackProd) temp[sellerNumber - 1].getProducts()[productNumber - 1]).getExtraPay() +
                            temp[sellerNumber - 1].getProducts()[productNumber - 1].getPrice();
                    System.out.println("your total price for the product '"+
                            temp[sellerNumber - 1].getProducts()[productNumber - 1].getName() +
                            "' is: "+FinalPay+"$");
                    p.setSpecialProd(true);
                }
                data.getBuyers()[buyerNumber - 1].addProduct(p);
            }
            else{
                Product p = data.getSellers()[sellerNumber - 1].getProducts()[productNumber - 1];
                data.getBuyers()[buyerNumber - 1].addProduct(p);
            }

        }
    }

    // Function to process payment for a buyer (implementation incomplete)
    static void payment() {
        String answer;
        float sum = 0;
        for (int i = 1; i <= data.getLogicSizeBuyers(); i++) {
            System.out.println(i + ") " + data.getBuyers()[i - 1].getName());
        }
        int buyerNumber = ExceptionCheckDomain("Enter buyer number for payment: ",data.getLogicSizeBuyers(),1 );// checks if input for the selected buyer is valid
        try{
            if(data.getBuyers()[buyerNumber-1].getLogicSizeProduct()!=0) {
                System.out.println("buyer name: " + data.getBuyers()[buyerNumber - 1].getName());
                sum = data.getSum()+data.getBuyers()[buyerNumber - 1].paymentPrice();
                System.out.println("your total payment is: " + sum + "$");
                Product[] cart = data.getBuyers()[buyerNumber - 1].getProducts();
                data.getBuyers()[buyerNumber - 1].addPaymentHistory(cart);
                Product[] newCart = new Product[0];
                data.getBuyers()[buyerNumber - 1].setProducts(Arrays.copyOf(newCart, 0));
                data.getBuyers()[buyerNumber - 1].setLogicSizeProduct(0);
            }else{
                throw new Exception("Your cart is EMPTY! please go add products and than come back to pay");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Function to display all buyers' data
    static void showBuyersData() {
        if (data.getLogicSizeBuyers() > 0) { // Check if there are any buyers
            Buyer[] temp = Arrays.copyOfRange(data.getBuyers(), 0, data.getLogicSizeBuyers());
            Arrays.sort(temp);
            for (Buyer buyer : temp) {
                System.out.println("\n"+buyer.toString()); // Print each Buyer's info
            }
            System.out.println();
        } else {
            System.out.println("No buyer yet.");
        }
    }

    // Function to display all sellers' data
    static void showSellersData() {
        if (data.getLogicSizeSellers() > 0) { // Check if there are any seller's
            Seller[] temp = Arrays.copyOfRange(data.getSellers(), 0, data.getLogicSizeSellers());
            Arrays.sort(temp);
            for (Seller seller : temp) {
                System.out.println("\n"+seller.toString()); // Print each seller's info
            }
            System.out.println();
        } else {
            System.out.println("No seller yet.");
        }
    }

    static void printAllProductByType() {
        int count =0;
        int index = ExceptionCheckDomain("1) Children\n2) Electricity\n3) Office\n4) Clothing\n" +
                "enter the number of the category: ",4, 1);// checks if input for the selected product type is valid
        Product.Category[] categories = Product.Category.values();
        for (int i = 0; i < data.getLogicSizeSellers(); i++) {
            for (int j = 0; j < data.getSellers()[i].getLogicSizeProduct(); j++) {
                if (data.getSellers()[i].getProducts()[j].getCategory() == categories[index - 1]) {
                    System.out.println(data.getSellers()[i].getProducts()[j].toString());
                    count++;
                }
            }
        }
        if(count==0){
            System.out.println("there aren't any products from the type you chose");
        }
    }

    static void changeCart() {
        int input = ExceptionCheckDomain("would you like to delete your current cart and " +
                "replace it with the one you have in history?\n" +
                "Enter 1 to continue, enter 0 to exit: ", 1,0); // checks if user wants to switch his cart

        if (input == 1) {
            for (int i = 1; i <= data.getLogicSizeBuyers(); i++) {
                System.out.println(i + ")" + data.getBuyers()[i - 1].getName());
            }
            int index = ExceptionCheckDomain("Pick your username's number: ", data.getLogicSizeBuyers(), 1);// checks if input for the selected buyer is valid
            if(data.getBuyers()[index - 1].getLogicSizePaymentHistory()!=0) {
                for (int j = 1; j <= data.getBuyers()[index - 1].
                        getLogicSizePaymentHistory(); j++) {
                    System.out.println(j + ")" + data.getBuyers()[index - 1].
                            getPaymentHistory()[j - 1].toString());
                }
                input = ExceptionCheckDomain("Pick the number of the cart you want to restore: ",
                        data.getBuyers()[index - 1].getLogicSizePaymentHistory(), 1);// checks if input for the selected cart from history is valid
                boolean stop = true;
                int count = 0;
                Product[] newCart = new Product[data.getBuyers()[index - 1].getPaymentHistory()[input - 1].getProducts().length];
                for (int i = 0; i < newCart.length && data.getBuyers()[index - 1].getPaymentHistory()[input - 1].getProducts()[i] != null; i++) {
                    newCart[i] = data.getBuyers()[index - 1].getPaymentHistory()[input - 1].getProducts()[i].clone();
                    count++;
                }
                data.getBuyers()[index - 1].setProducts(newCart);
                data.getBuyers()[index - 1].setLogicSizeProduct(count);
            }else{
                System.out.println("there haven't been any purchases in the Payment History yet");
            }
        }
    }

    // Function to handle user menu selection and perform corresponding operations
    static void operation(int selection) {
        switch (selection) {
            case 0: // Exit the program
                s.close();
                isMainRunning = false;
                break;
            case 1: // Add seller to the sellers array
                addSeller();
                break;
            case 2: // Add buyer to the buyers array
                addBuyer();
                break;
            case 3: // Add product to seller
                addProductToSeller();
                break;
            case 4: // Add product to buyer
                addProductToBuyer();
                break;
            case 5: // Process payment for buyer
                payment();
                break;
            case 6: // Show all buyers' information
                showBuyersData();
                break;
            case 7: // Show all sellers' information
                showSellersData();
                break;
            case 8:
                printAllProductByType();
                break;
            case 9:
                changeCart();
                break;
        }
    }

    // name: Yuval Alkalay  id: 207962770
// name: Almog Dinur    id: 211627054
    // save. function to display the menu and take user input for operations
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        while (isMainRunning) {
            try {
                // Display menu options
                System.out.println("0 : Exit");
                System.out.println("1 : Add seller");
                System.out.println("2 : Add buyer");
                System.out.println("3 : Add product to seller");
                System.out.println("4 : Add product to buyer");
                System.out.println("5 : Payment for buyer");
                System.out.println("6 : Show all buyer's information");
                System.out.println("7 : Show all seller's information");
                System.out.println("8 : Show all products by type");
                System.out.println("9 : Switch current cart with cart from history");
                System.out.print("Please enter a number of selection from the menu: ");

                // Get user's menu selection
                int select = s.nextInt();
                if(select>=0 && select<=9){
                    operation(select);// Perform operation based on user's selection
                }
                else{
                    throw new Exception("Invalid input. the number must be between 0 to 9.");
                }
            }catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                s.nextLine();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}