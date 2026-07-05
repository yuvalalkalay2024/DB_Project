/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package DB_Project;
/**
 *
 * @author yuval, adi, may
 */

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class DB_Project extends GenericFunctions{
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

        for (int i = 1; i <= data.getLogicSizeSellers(); i++) {
            System.out.println(i + ") " + data.getSellers()[i - 1].getName());
        }
        int sellerNumber = ExceptionCheckDomain("enter the seller's number: ",data.getLogicSizeSellers(), 1); // checks if input for the selected seller is valid
        Seller selectedSeller = data.getSellers()[sellerNumber - 1];
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
            data.addProductToSeller(selectedSeller.getUserId(), newProduct);
        } else {
            Product newProduct = new Product(product, price, categories[index - 1], false);
            data.getSellers()[sellerNumber - 1].addProduct(newProduct);
            data.addProductToSeller(selectedSeller.getUserId(), newProduct);
        }
    }

    // Function to add a product to a buyer (implementation incomplete)
    static void addProductToBuyer() {
        int counter = 0;
        Seller[] temp = new Seller[0];
        Seller[] sellers = new Seller[0];
        Buyer[] buyers = new Buyer[0];
        int LogicSizeSellers = data.getLogicSizeSellers();
        int LogicSizeBuyers = data.getLogicSizeBuyers();
        sellers = Arrays.copyOf(data.getSellers(), LogicSizeSellers);
        buyers = Arrays.copyOf(data.getBuyers(), LogicSizeBuyers);

        for (int i = 1; i <= LogicSizeBuyers; i++) {
            System.out.println(i + ") " + data.getBuyers()[i - 1].getName());
        }
        int buyerNumber = ExceptionCheckDomain("Enter buyer number: ",data.getLogicSizeBuyers(),1);// checks if input for the selected buyer is valid
        for (int i = 1; i <= data.getLogicSizeSellers(); i++) {//prints only the sellers that have products to sell
            if (sellers[i - 1].getLogicSizeProduct() != 0) {
                counter++;
                System.out.println(counter + ") " + sellers[i - 1].getName());
                temp = Arrays.copyOf(temp, counter);
                temp[counter - 1] = sellers[i - 1];
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
                // buyers[buyerNumber - 1]
                data.addProductToBuyerCart(buyers[buyerNumber - 1].getUserId(), temp[sellerNumber - 1].getProducts()[productNumber - 1].getID(), 1);
            }
            else{
                // Product p = sellers[sellerNumber - 1].getProducts()[productNumber - 1];
                data.addProductToBuyerCart(buyers[buyerNumber - 1].getUserId(), temp[sellerNumber - 1].getProducts()[productNumber - 1].getID(), 1);
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
                System.out.println("\n" + buyer.toString()); // Print each Buyer's info
                
                // התוספת שלנו - הדפסת העגלה החיה ישירות ממסד הנתונים!
                data.printBuyerCart(buyer.getUserId()); 
                data.printBuyerPaymentHistory(buyer.getUserId());
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
        
        int index = ExceptionCheckDomain("1) Children\n2) Electricity\n3) Office\n4) Clothing\n" +
                "enter the number of the category: ", 4, 1); // checks if input for the selected product type is valid

        // שליפת המוצרים ישירות ממסד הנתונים בעזרת הפונקציה שכתבנו
        Product[] products = data.getProductByCategorie(index);
        
        // בדיקה אם המערך ריק (במקום להשתמש במשתנה count)
        if (products.length == 0) {
            System.out.println("there aren't any products from the type you chose");
        } else {
            // מעבר על המערך בעזרת .length והדפסת כל מוצר
            for (int i = 0; i < products.length; i++) {
                System.out.println(products[i].toString());
            }
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
/////////////////////////////////////////////////////////////////////////////////////////////
    static void deleteSeller(){
        System.out.println("Enter the ID of the seller you want to delete:");
        int idToDelete = s.nextInt();
        s.nextLine(); // ניקוי החוצץ
        
        // קריאה לפונקציה מתוך אובייקט הנתונים שלך (למשל data)
        data.deleteSellerById(idToDelete);
    }
                
    static void deleteBuyer(){
        System.out.println("Enter the ID of the buyer you want to delete:");
        int idToDelete = s.nextInt();
        s.nextLine(); // ניקוי החוצץ
        
        // קריאה לפונקציה מתוך אובייקט הנתונים שלך (למשל data)
        data.deleteBuyerById(idToDelete);
    }
                
    static void deleteProductToSeller(){
        System.out.println("Enter your Seller ID:");
        int sellerId = s.nextInt();
        
        System.out.println("Enter the ID of the product you want to delete:");
        int productId = s.nextInt();
        s.nextLine(); // ניקוי החוצץ (Buffer) אחרי קליטת מספר
        
        // קריאה לפונקציה מתוך אובייקט הנתונים שלך (לדוגמה data)
        data.deleteProductBySeller(productId, sellerId);
    }
                
    static void deleteProductToBuyer(){
        System.out.println("Enter your Buyer ID:");
        int buyerId = s.nextInt();
        
        System.out.println("Enter the ID of the product you want to delete:");
        int productId = s.nextInt();
        s.nextLine(); // ניקוי החוצץ (Buffer) אחרי קליטת מספר
        
        // קריאה לפונקציה מתוך אובייקט הנתונים שלך (לדוגמה data)
        data.deleteProductBySeller(productId, buyerId);
    }
                
    static void updateSeller(){
        System.out.println("Enter the ID of the seller you want to update:");
        int sellerId = s.nextInt();
        s.nextLine(); // ניקוי החוצץ (Buffer) לאחר קליטת מספר
        
        System.out.println("Enter the new username:");
        String newUsername = s.nextLine();
        
        System.out.println("Enter the new password:");
        String newPassword = s.nextLine();
        
        // קריאה לפונקציה (בהנחה שלמופע של המחלקה קוראים data)
        data.updateSeller(sellerId, newUsername, newPassword);
    }
                
    static void updateBuyer(){
            System.out.println("Enter the ID of the buyer you want to update:");
        int buyerId = s.nextInt();
        s.nextLine(); // ניקוי החוצץ (Buffer) לאחר קליטת מספר
        
        System.out.println("Enter the new username:");
        String newUsername = s.nextLine();
        
        System.out.println("Enter the new password:");
        String newPassword = s.nextLine();
        
        // קריאה לפונקציה (בהנחה שלמופע של המחלקה קוראים data)
        data.updateSeller(buyerId, newUsername, newPassword);
    }
                
    static void updateProductToSeller(){
        System.out.println("Enter your Seller ID:");
        int sellerId = s.nextInt();
        
        System.out.println("Enter the ID of the product you want to update:");
        int productId = s.nextInt();
        s.nextLine(); // ניקוי החוצץ (Buffer) לאחר קליטת מספר
        
        System.out.println("Enter the new product name:");
        String newName = s.nextLine();
        
        System.out.println("Enter the new price:");
        double newPrice = s.nextDouble();
        s.nextLine(); // ניקוי החוצץ
        
        System.out.println("Enter the new category (Children, Electricity, Office, Clothing):");
        String newCategory = s.nextLine();
        
        // קריאה לפונקציה מתוך אובייקט הנתונים שלך (לדוגמה data)
        data.updateProductBySeller(productId, sellerId, newName, newPrice, newCategory);
    }
            
    static void updateProductToBuyer(){
        System.out.println("Enter Buyer ID:");
        int buyerId = s.nextInt();
        
        System.out.println("Enter the ID of the product in the cart you want to update:");
        int productId = s.nextInt();
        
        System.out.println("Enter the new quantity:");
        int newQuantity = s.nextInt();
        s.nextLine(); // ניקוי החוצץ (Buffer) לאחר קליטת מספר
        
        // קריאה לפונקציה מתוך אובייקט הנתונים שלך (לדוגמה data)
        data.updateProductQuantityForBuyer(buyerId, productId, newQuantity);
    }

    // Wrapper functions for the meaningful query reports (call through to the data object)
    static void reportDiverseBuyers() {
        data.reportDiverseBuyers();
    }

    static void reportLazyBuyers() {
        data.reportLazyBuyers();
    }

    static void reportTopSeller() {
        data.reportTopSeller();
    }

    static void reportMostDiverseSeller() {
        data.reportMostDiverseSeller();
    }

    static void reportDormantSellers() {
        data.reportDormantSellers();
    }

    static void reportBestSellingProducts() {
        data.reportBestSellingProducts();
    }

    static void reportSalesPerSeller() {
        data.reportSalesPerSeller();
    }

    static void reportAbandonedProducts() {
        data.reportAbandonedProducts();
    }

    static void reportRevenueByCategory() {
        data.reportRevenueByCategory();
    }

    static void reportWhaleOrders() {
        data.reportWhaleOrders();
    }
/////////////////////////////////////////////////////////////////////////////////////////////


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
            case 5:
                deleteSeller();
                break;
            case 6:
                deleteBuyer();
                break;
            case 7:
                deleteProductToSeller();
                break;
            case 8:
                deleteProductToBuyer();
                break;
            case 9:
                updateSeller();
                break;
            case 10:
                updateBuyer();
                break;
            case 11:
                updateProductToSeller();
                break;
            case 12:
                updateProductToBuyer();
                break;
            case 13: // Process payment for buyer
                payment();
                break;
            case 14: // Show all buyers' information
                showBuyersData();
                break;
            case 15: // Show all sellers' information
                showSellersData();
                break;
            case 16:
                printAllProductByType();
                break;
            case 17:
                changeCart();
                break;
            case 18:
                DatabaseTester.runAllMenuOptions(data);
                break;
            case 19:
                reportsMenu();
                break;
            default:
                System.out.println("Invalid selection. Please try again.");
                break;
        }
    }

    // Function to handle the reports submenu selection and perform corresponding operations
    static void reportsOperation(int selection) {
        switch (selection) {
            case 0: // Return to main menu
                break;
            case 1:
                reportDiverseBuyers();
                break;
            case 2:
                reportLazyBuyers();
                break;
            case 3:
                reportTopSeller();
                break;
            case 4:
                reportMostDiverseSeller();
                break;
            case 5:
                reportDormantSellers();
                break;
            case 6:
                reportBestSellingProducts();
                break;
            case 7:
                reportSalesPerSeller();
                break;
            case 8:
                reportAbandonedProducts();
                break;
            case 9:
                reportRevenueByCategory();
                break;
            case 10:
                reportWhaleOrders();
                break;
            default:
                System.out.println("Invalid selection. Please try again.");
                break;
        }
    }

    // Function to display the reports submenu and take user input until the user chooses to go back
    static void reportsMenu() {
        boolean isReportsRunning = true;
        while (isReportsRunning) {
            try {
                System.out.println("0  : Back to main menu");
                System.out.println("1  : Report buyers who bought from diverse categories");
                System.out.println("2  : Report lazy buyers (low/no purchase activity)");
                System.out.println("3  : Report the top seller by revenue");
                System.out.println("4  : Report the seller with the most diverse products");
                System.out.println("5  : Report dormant sellers (no recent sales)");
                System.out.println("6  : Report best-selling products");
                System.out.println("7  : Report sales per seller");
                System.out.println("8  : Report abandoned products (in carts, never bought)");
                System.out.println("9  : Report revenue by category");
                System.out.println("10 : Report whale orders (largest purchases)");

                System.out.print("Please enter a number of selection from the reports menu: ");

                int select = s.nextInt();
                if (select >= 0 && select <= 10) {
                    reportsOperation(select);
                    if (select == 0) {
                        isReportsRunning = false;
                    }
                } else {
                    throw new Exception("Invalid input. the number must be between 0 to 10.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                s.nextLine();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // name: Yuval Alkalay  id: 207962770
    // name: Adi Simhony    id: 206350654
    // name: May Shehory      id: 208293902
    // save. function to display the menu and take user input for operations
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        while (isMainRunning) {
            try {
                // Display menu options
                System.out.println("0  : Exit");
                System.out.println("1  : Add seller");
                System.out.println("2  : Add buyer");
                System.out.println("3  : Add product to seller");
                System.out.println("4  : Add product to buyer");
                System.out.println("5  : delete seller");
                System.out.println("6  : delete buyer");
                System.out.println("7  : delete product to seller");
                System.out.println("8  : delete product to buyer");
                System.out.println("9  : update seller");
                System.out.println("10 : update buyer");
                System.out.println("11 : update product to seller");
                System.out.println("12 : update product to buyer");
                System.out.println("13 : Payment for buyer");
                System.out.println("14 : Show all buyer's information");
                System.out.println("15 : Show all seller's information");
                System.out.println("16 : Show all products by type");
                System.out.println("17 : Switch current cart with cart from history");
                System.out.println("18 : Test ");
                System.out.println("19 : Reports menu");

                System.out.print("Please enter a number of selection from the menu: ");

                // Get user's menu selection
                int select = s.nextInt();
                if(select>=0 && select<=19){
                    operation(select);// Perform operation based on user's selection
                }
                else{
                    throw new Exception("Invalid input. the number must be between 0 to 19.");
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

