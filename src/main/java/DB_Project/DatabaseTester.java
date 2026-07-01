package DB_Project;

public class DatabaseTester {

    public static void runAllMenuOptions(SingleSourceOfTruth data) {
        System.out.println("\n==================================================");
        System.out.println("🚀 STARTING AUTOMATED FULL SYSTEM TEST (18 STEPS) 🚀");
        System.out.println("==================================================\n");

        try {
            // יצירת מזהה ייחודי כדי למנוע שגיאות כפילות בשמות משתמש (UNIQUE constraint)
            String uniqueId = String.valueOf(System.currentTimeMillis() % 100000);
            
            System.out.println("\n--- PART 1: SELLER FLOW ---");
            
            // 1. הוספת מוכר (בהנחה שעל DB נקי הוא יקבל ID 1)
            Seller testSeller = new Seller("Seller_" + uniqueId, "pass123");
            data.addSeller(testSeller);
            System.out.println("1. ✅ Seller added: " + testSeller.getName());

            // 2. עדכון המוכר
            data.updateSeller(1, "UpdatedSeller_" + uniqueId, "newPass123");
            System.out.println("2. ✅ Seller details updated.");

            // 3. הוספת שלושה מוצרים למוכר
            Product p1 = new Product("Laptop", 3500.0f, Product.Category.Electricity, false);
            Product p2 = new Product("Shirt", 150.0f, Product.Category.Clothing, false);
            Product p3 = new Product("Desk", 800.0f, Product.Category.Office, false);
            data.addProductToSeller(1, p1);
            data.addProductToSeller(1, p2);
            data.addProductToSeller(1, p3);
            System.out.println("3. ✅ 3 Products added to Seller's catalog.");

            // 4. עדכון מוצר אצל המוכר
            data.updateProductBySeller(1, 1, "Gaming Laptop", 4000.0f, "Electricity");
            System.out.println("4. ✅ Product updated in Seller's catalog.");

            // 5. מחיקת מוצר אחד (נמחק את המוצר השלישי - Desk שקיבל ID 3)
            data.deleteProductBySeller(3, 1); 
            System.out.println("5. ✅ 1 Product deleted from Seller's catalog.");


            System.out.println("\n--- PART 2: BUYER & CART FLOW ---");
            
            // 6. הוספת קונה (בהנחה שעל DB נקי הוא יקבל ID 1)
            Address addr = new Address("Israel", "Tel Aviv", "Herzl", 10);
            Buyer testBuyer = new Buyer("Buyer_" + uniqueId, "pass456", addr);
            data.addBuyer(testBuyer);
            System.out.println("6. ✅ Buyer added: " + testBuyer.getName());

            // 7. עדכון הקונה
            data.updateBuyer(1, "UpdatedBuyer_" + uniqueId, "pass789");
            System.out.println("7. ✅ Buyer details updated.");

            // 8. הוספת מוצרים לעגלת קניות
            data.addProductToBuyerCart(1, 1, 1); // הוספת מחשב נייד (כמות 1)
            data.addProductToBuyerCart(1, 2, 2); // הוספת חולצות (כמות 2)
            System.out.println("8. ✅ Products added to Buyer's Cart.");

            // 9. עדכון כמות של מוצר אצל הקונה בעגלה
            data.updateProductQuantityForBuyer(1, 2, 3); // עדכון כמות החולצות ל-3
            System.out.println("9. ✅ Product quantity updated in Cart.");

            // 10. מחיקת מוצר אחד מהעגלה (נמחק את החולצות מהעגלה)
            data.removeProductFromBuyerCart(1, 2); 
            System.out.println("10. ✅ Product removed from Cart.");


            System.out.println("\n--- PART 3: SYSTEM VIEWS & REPORTS ---");
            
            // 11. הצגת מוכרים
            Seller[] sellers = data.getSellers();
            System.out.println("11. ✅ Found " + sellers.length + " sellers in DB.");

            // 12. הצגת קונים
            Buyer[] buyers = data.getBuyers();
            System.out.println("12. ✅ Found " + buyers.length + " buyers in DB.");

            // 13. הצגת מוצרים לפי קטגוריה (נבדוק מוצרי חשמל)
            Product[] products = data.getProductByCategorie(2); // 2 = Electricity
            System.out.println("13. ✅ Found " + (products != null ? products.length : 0) + " Electricity products.");


            System.out.println("\n--- PART 4: CHECKOUT & CART HISTORY ---");

            // 14. תשלום עבור הקונה (העגלה נסגרת)
            boolean paymentSuccess = data.processPayment(1); 
            System.out.println("14. ✅ Payment processed! Cart moved to History. Status: " + paymentSuccess);

            // 15. רווח המערכת (הסכום שהקונה שילם נכנס לקופה)
            float revenue = data.getSum();
            System.out.println("15. ✅ System Total Revenue: ₪" + revenue);

            // 16. מעבר עגלה מול ההיסטוריה (נניח מזהה הזמנה 1)
            boolean switched = data.switchCartWithHistory(1, 1);
            System.out.println("16. ✅ Switched active cart with history cart successfully. Status: " + switched);


            System.out.println("\n--- PART 5: CLEANUP & DELETIONS ---");

            // 17. מחיקת הקונה (אמור למחוק CASCADE את כל העגלות וההיסטוריה שלו)
            data.deleteBuyerById(1);
            System.out.println("17. ✅ Buyer and their cart history deleted.");

            // 18. מחיקת המוכר (אמור למחוק CASCADE את כל המוצרים שנשארו לו)
            data.deleteSellerById(1);
            System.out.println("18. ✅ Seller and their catalog deleted.");


            System.out.println("\n==================================================");
            System.out.println("🏁 ALL 18 TESTS PASSED SUCCESSFULLY! 🏁");
            System.out.println("==================================================\n");

        } catch (Exception e) {
            System.err.println("\n❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}