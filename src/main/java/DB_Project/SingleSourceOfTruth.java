package DB_Project;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SingleSourceOfTruth {

    public SingleSourceOfTruth() {
        // אין צורך לאתחל מערכים יותר - הנתונים יושבים בבסיס הנתונים!
    }

    // ==========================================
    // 1. הוספת משתמשים (קונים ומוכרים)
    // ==========================================

    public void addBuyer(Buyer buyer) {
        String insertUserSql = "INSERT INTO Users (username, password, role) VALUES (?, ?, 'BUYER') RETURNING user_id";
        String insertAddressSql = "INSERT INTO Addresses (user_id, country, city, street, house_number) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // פותחים טרנזקציה (כדי לוודא שהמשתמש והכתובת נשמרים יחד)

            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql)) {
                userStmt.setString(1, buyer.getName());
                userStmt.setString(2, buyer.getPassWord());
                
                ResultSet rs = userStmt.executeQuery();
                if (rs.next()) {
                    int newUserId = rs.getInt("user_id");
                    
                    // שמירת הכתובת של הקונה בטבלת Addresses
                    try (PreparedStatement addressStmt = conn.prepareStatement(insertAddressSql)) {
                        Address addr = buyer.getAddress();
                        addressStmt.setInt(1, newUserId);
                        addressStmt.setString(2, addr.getCountry());
                        addressStmt.setString(3, addr.getCity());
                        addressStmt.setString(4, addr.getStreet());
                        addressStmt.setInt(5, addr.getHouseNumber());
                        addressStmt.executeUpdate();
                    }
                }
                conn.commit(); // מאשרים את שמירת הנתונים
                System.out.println("Buyer added to database successfully!");
            } catch (SQLException e) {
                conn.rollback(); // במקרה של שגיאה - מבטלים את שתי הפעולות
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSeller(Seller seller) {
        String insertUserSql = "INSERT INTO Users (username, password, role) VALUES (?, ?, 'SELLER')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertUserSql)) {
            
            stmt.setString(1, seller.getName());
            stmt.setString(2, seller.getPassWord());
            stmt.executeUpdate();
            
            System.out.println("Seller added to database successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding seller: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. בדיקת קיום משתמשים (Validation)
    // ==========================================

    public boolean isBuyerExist(String name) {
        String sql = "SELECT 1 FROM Users WHERE username = ? AND role = 'BUYER'";
        return checkExistence(sql, name);
    }

    public boolean isSellerExist(String name) {
        String sql = "SELECT 1 FROM Users WHERE username = ? AND role = 'SELLER'";
        return checkExistence(sql, name);
    }

    // פונקציית עזר פרטית למניעת שכפול קוד בבדיקות קיום
    private boolean checkExistence(String sql, String username) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // אם חזרה שורה, המשתמש קיים
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // 3. שליפת נתונים (החלפה של Getters מקוריים)
    // ==========================================

    // מביא את כמות הקונים ישירות מה-DB (מחליף את logicSizeBuyers)
    public int getLogicSizeBuyers() {
        return getCountByRole("BUYER");
    }

    // מביא את כמות המוכרים ישירות מה-DB (מחליף את logicSizeSellers)
    public int getLogicSizeSellers() {
        return getCountByRole("SELLER");
    }

    private int getCountByRole(String role) {
        String sql = "SELECT COUNT(*) FROM Users WHERE role = ?::user_role";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // פונקציה להמרת נתוני ה-DB חזרה למערך Buyer[] (כדי לשמור על תאימות לקוד הקיים שלך)
    public Buyer[] getBuyers() {
        List<Buyer> buyersList = new ArrayList<>();
        // מבצעים JOIN כדי להביא גם את פרטי המשתמש וגם את הכתובת שלו
        String sql = "SELECT u.username, u.password, a.country, a.city, a.street, a.house_number " +
                     "FROM Users u LEFT JOIN Addresses a ON u.user_id = a.user_id WHERE u.role = 'BUYER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Address addr = new Address(rs.getString("country"), rs.getString("city"), 
                                           rs.getString("street"), rs.getInt("house_number"));
                Buyer b = new Buyer(rs.getString("username"), rs.getString("password"), addr);
                buyersList.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // ממירים את הרשימה חזרה למערך כדי שה-App.java שלך ימשיך לעבוד כרגיל
        return buyersList.toArray(new Buyer[0]);
    }

    public Seller[] getSellers() {
        List<Seller> sellersList = new ArrayList<>();
        String sql = "SELECT username, password FROM Users WHERE role = 'SELLER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Seller s = new Seller(rs.getString("username"), rs.getString("password"));
                sellersList.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sellersList.toArray(new Seller[0]);
    }

    // סכום הרווחים הכולל (מחליף את משתנה ה-sum בזיכרון)
    public float getSum() {
        String sql = "SELECT COALESCE(SUM(p.price + COALESCE(sp.extra_pay, 0)), 0) AS total_revenue " +
                     "FROM Order_Products op " +
                     "JOIN Products p ON op.product_id = p.product_id " +
                     "LEFT JOIN Special_Products sp ON p.product_id = sp.product_id";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getFloat("total_revenue");
            }
        } catch (SQLException e) {
               e.printStackTrace();
        }
        return 0f;
    }

    public boolean deleteSellerById(int sellerId){
        // השאילתה מוודאת שאנחנו מוחקים רק משתמש שהוא באמת מוכר ולא קונה
        String sql = "DELETE FROM Users WHERE user_id = ? AND role = 'SELLER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // הצבת ה-ID שקיבלנו לתוך סימן השאלה בשאילתה
            stmt.setInt(1, sellerId);
            
            // הרצת השאילתה ובדיקה כמה שורות הושפעו
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Seller with ID " + sellerId + " deleted successfully (along with their products).");
                return true;
            } else {
                System.out.println("No seller found with ID " + sellerId + ".");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting seller: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
                
    public boolean  deleteBuyerById(int buyerId){
                // השאילתה מוודאת שאנחנו מוחקים רק משתמש שהוא באמת מוכר ולא קונה
        String sql = "DELETE FROM Users WHERE user_id = ? AND role = 'BUYER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // הצבת ה-ID שקיבלנו לתוך סימן השאלה בשאילתה
            stmt.setInt(1, buyerId);
            
            // הרצת השאילתה ובדיקה כמה שורות הושפעו
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Buyer with ID " + buyerId + " deleted successfully (along with their products).");
                return true;
            } else {
                System.out.println("No buyer found with ID " + buyerId + ".");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting buyer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
                
    public boolean deleteProductBySeller(int productId, int sellerId){
        String sql = "DELETE FROM Products WHERE product_id = ? AND seller_id = ?";

                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    stmt.setInt(1, productId);
                    stmt.setInt(2, sellerId);
                    
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        System.out.println("Product ID " + productId + " was successfully deleted from your catalog.");
                        return true;
                    } else {
                        System.out.println("Failed to delete. Either the product ID " + productId + " doesn't exist, or it doesn't belong to Seller ID " + sellerId + ".");
                        return false;
                    }
                    
                } catch (SQLException e) {
                    System.err.println("Error deleting product: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
    }
                
    public boolean deleteProductByBuyer(int productId, int buyerId){
                String sql = "DELETE FROM Products WHERE product_id = ? AND buyer_id = ?";

                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    stmt.setInt(1, productId);
                    stmt.setInt(2, buyerId);
                    
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        System.out.println("Product ID " + productId + " was successfully deleted from your catalog.");
                        return true;
                    } else {
                        System.out.println("Failed to delete. Either the product ID " + productId + " doesn't exist, or it doesn't belong to Buyer ID " + buyerId + ".");
                        return false;
                    }
                    
                } catch (SQLException e) {
                    System.err.println("Error deleting product: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
    }
                
    public boolean  updateSeller(int sellerId, String newUsername, String newPassword){
        // השאילתה מעדכנת רק משתמשים שהם מסוג SELLER
        String sql = "UPDATE Users SET username = ?, password = ? WHERE user_id = ? AND role = 'SELLER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newUsername);
            stmt.setString(2, newPassword);
            stmt.setInt(3, sellerId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Seller ID " + sellerId + " was successfully updated.");
                return true;
            } else {
                System.out.println("Failed to update. Seller ID " + sellerId + " not found.");
                return false;
            }
            
        } catch (SQLException e) {
            // 23505 הוא קוד השגיאה של PostgreSQL למצב שבו מפרים אילוץ UNIQUE
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Error: The username '" + newUsername + "' is already taken by another user.");
            } else {
                System.err.println("Error updating seller: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }
                
    public boolean updateBuyer(int buyerId, String newUsername, String newPassword){
        // השאילתה מעדכנת רק משתמשים שהם מסוג SELLER
        String sql = "UPDATE Users SET username = ?, password = ? WHERE user_id = ? AND role = 'BUYER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newUsername);
            stmt.setString(2, newPassword);
            stmt.setInt(3, buyerId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Buyer ID " + buyerId + " was successfully updated.");
                return true;
            } else {
                System.out.println("Failed to update. Buyer ID " + buyerId + " not found.");
                return false;
            }
            
        } catch (SQLException e) {
            // 23505 הוא קוד השגיאה של PostgreSQL למצב שבו מפרים אילוץ UNIQUE
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Error: The username '" + newUsername + "' is already taken by another user.");
            } else {
                System.err.println("Error updating buyer: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }
                
    public boolean  updateProductBySeller(int productId, int sellerId, String newName, double newPrice, String newCategory){
        // שימוש ב- ::product_category כדי להתאים ל-ENUM שיצרנו בבסיס הנתונים
        String sql = "UPDATE Products SET name = ?, price = ?, category = ?::product_category WHERE product_id = ? AND seller_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newName);
            stmt.setDouble(2, newPrice);
            stmt.setString(3, newCategory);
            stmt.setInt(4, productId);
            stmt.setInt(5, sellerId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Product ID " + productId + " was successfully updated.");
                return true;
            } else {
                System.out.println("Failed to update. Either Product ID " + productId + " doesn't exist, or it doesn't belong to Seller ID " + sellerId + ".");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
            
    public boolean  updateProductbyBuyer(int buyerId, int productId, int newQuantity){
        // שים לב: החלף את 'Cart_Products' בשם הטבלה האמיתי שלך מתוך קובץ ה-DDL
        // ייתכן גם שהעמודות נקראות אצלך cart_id ולא buyer_id, תלוי איך מידלת את העגלה.
        String sql = "UPDATE Cart_Products SET quantity = ? WHERE buyer_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, buyerId);
            stmt.setInt(3, productId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Successfully updated quantity to " + newQuantity + " for Product ID " + productId + " in Buyer ID " + buyerId + "'s cart.");
                return true;
            } else {
                System.out.println("Failed to update. Product ID " + productId + " was not found in this buyer's cart.");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating product quantity for buyer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

public Product[] getProductByCategorie(int category) {
        String categoryStr = "";
        
        switch (category) {
            case 1:
                categoryStr = "Children";
                break;
            case 2:
                categoryStr = "Electricity";
                break;
            case 3:
                categoryStr = "Office";
                break;
            case 4:
                categoryStr = "Clothing";
                break;
            default:
                System.out.println("Invalid category selected.");
                return new Product[0];
        }
        
        // השאילתה - שים לב לשימוש ב-?
        String sql = "SELECT * FROM products WHERE category = ?::product_category";
        
        List<Product> productsList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // מציבים את הקטגוריה בשאילתה
            stmt.setString(1, categoryStr);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // 1. חילוץ הנתונים מהעמודות בטבלה
                    String name = rs.getString("name");
                    float price = rs.getFloat("price");
                    
                    // 2. המרת הקטגוריה (String) מה-DB ל-Enum של המחלקה
                    Product.Category catEnum = Product.Category.valueOf(rs.getString("category"));
                    
                    // הערה: אני מניח שאין עמודת "isSpecialProd" בטבלת products הרגילה, אז נעביר false בינתיים.
                    // אם יש עמודה כזו, אפשר לשנות ל: boolean isSpecial = rs.getBoolean("is_special");
                    boolean isSpecial = false; 
                    
                    // 3. יצירת האובייקט בעזרת הבנאי שלך והוספתו לרשימה
                    Product p = new Product(name, price, catEnum, isSpecial);
                    productsList.add(p);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching products by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        // החזרת המערך (אם אין תוצאות, יוחזר מערך באורך 0)
        return productsList.toArray(new Product[0]);
    }

}