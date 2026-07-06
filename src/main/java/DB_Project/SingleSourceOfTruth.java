package DB_Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SingleSourceOfTruth {

    public SingleSourceOfTruth() {
        // אין צורך לאתחל מערכים יותר - הנתונים יושבים בבסיס הנתונים!
    }

    // ==========================================
    // 1. הוספת משתמשים (קונים ומוכרים)
    // ==========================================

public int addBuyer(Buyer buyer) {
        String insertUserSql = "INSERT INTO Users (username, password, role) VALUES (?, ?, 'BUYER') RETURNING user_id";
        String insertAddressSql = "INSERT INTO Addresses (user_id, country, city, street, house_number) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql)) {
                userStmt.setString(1, buyer.getName());
                userStmt.setString(2, buyer.getPassWord());
                
                ResultSet rs = userStmt.executeQuery();
                if (rs.next()) {
                    int newUserId = rs.getInt("user_id"); // שומרים את ה-ID החדש שנוצר
                    
                    try (PreparedStatement addressStmt = conn.prepareStatement(insertAddressSql)) {
                        Address addr = buyer.getAddress();
                        addressStmt.setInt(1, newUserId);
                        addressStmt.setString(2, addr.getCountry());
                        addressStmt.setString(3, addr.getCity());
                        addressStmt.setString(4, addr.getStreet());
                        addressStmt.setInt(5, addr.getHouseNumber());
                        addressStmt.executeUpdate();
                    }
                    conn.commit(); 
                    System.out.println("Buyer added to database successfully! ID: " + newUserId);
                    return newUserId; // מחזירים את ה-ID למי שקרא לפונקציה!
                }
            } catch (SQLException e) {
                conn.rollback(); 
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // במקרה של כישלון
    }

public int addSeller(Seller seller) {
        String insertUserSql = "INSERT INTO Users (username, password, role) VALUES (?, ?, 'SELLER') RETURNING user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertUserSql)) {
            
            stmt.setString(1, seller.getName());
            stmt.setString(2, seller.getPassWord());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int newUserId = rs.getInt("user_id");
                System.out.println("Seller added to database successfully! ID: " + newUserId);
                return newUserId; // מחזירים את ה-ID למי שקרא לפונקציה!
            }
        } catch (SQLException e) {
            System.out.println("Error adding seller: " + e.getMessage());
        }
        return -1; // במקרה של כישלון
    }

    // ==========================================
    // 2. הוספת מוצרים (לקטלוג או לעגלה)
    // ==========================================

public int addProductToSeller(int sellerId, Product p) {
        String sql = "INSERT INTO Products (seller_id, name, price, category, is_special_prod) VALUES (?, ?, ?, ?::product_category, ?) RETURNING product_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sellerId);
            stmt.setString(2, p.getName());
            stmt.setDouble(3, p.getPrice());
            stmt.setString(4, p.getCategory().name());
            stmt.setBoolean(5, p.getisSpecialProd());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("product_id"); // מחזירים את ה-ID החדש שנוצר!
            }
        } catch (SQLException e) {
            System.err.println("Error adding product to seller: " + e.getMessage());
        }
        return -1; // במקרה של כישלון
    }

    public boolean addProductToBuyerCart(int buyerId, int productId, int quantity) {
        String sql = "INSERT INTO Cart_Products (buyer_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, buyerId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding product to cart: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // 3. בדיקת קיום משתמשים (Validation)
    // ==========================================

    public boolean isBuyerExist(String name) {
        String sql = "SELECT 1 FROM Users WHERE username = ? AND role = 'BUYER'";
        return checkExistence(sql, name);
    }

    public boolean isSellerExist(String name) {
        String sql = "SELECT 1 FROM Users WHERE username = ? AND role = 'SELLER'";
        return checkExistence(sql, name);
    }

    private boolean checkExistence(String sql, String username) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // 4. שליפת נתונים ודוחות
    // ==========================================

    public int getLogicSizeBuyers() {
        return getCountByRole("BUYER");
    }

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

    public Buyer[] getBuyers() {
        List<Buyer> buyersList = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.password, a.country, a.city, a.street, a.house_number " +
                     "FROM Users u LEFT JOIN Addresses a ON u.user_id = a.user_id WHERE u.role = 'BUYER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Address addr = new Address(rs.getString("country"), rs.getString("city"), 
                                           rs.getString("street"), rs.getInt("house_number"));
                Buyer b = new Buyer(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), addr);
                buyersList.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return buyersList.toArray(new Buyer[0]);
    }

    public Product[] getCartOfBuyer(int buyerID) {
        List<Product> cart = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.price, p.category, p.is_special_prod, sp.extra_pay " +
                     "FROM Cart_Products cp " +
                     "JOIN Products p ON cp.product_id = p.product_id " +
                     "LEFT JOIN Special_Products sp ON p.product_id = sp.product_id " +
                     "WHERE cp.buyer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, buyerID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // 1. המרה תקנית של ה-String מה-DB ל-Enum של Java
                    Product.Category cat = Product.Category.valueOf(rs.getString("category"));
                    boolean isSpecial = rs.getBoolean("is_special_prod");
                    Product p;

                    if (isSpecial) {
                        // שימוש בבנאי התקני + תוספת תשלום
                        p = new SpecialPackProd(
                                rs.getInt("product_id"), 
                                rs.getString("name"), 
                                rs.getFloat("price"), 
                                cat, // מעבירים את ה-Enum, לא String!
                                true, 
                                rs.getFloat("extra_pay")
                        );
                    } else {
                        // שימוש בבנאי התקני של Product
                        p = new Product(
                                rs.getInt("product_id"), 
                                rs.getString("name"), 
                                rs.getFloat("price"), 
                                cat, // מעבירים את ה-Enum, לא String!
                                false
                        );
                    }
                    
                    // 2. הוספה לרשימה המקומית (במקום למשתנה b שלא קיים)
                    cart.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // 3. החזרה נכונה של מערך מוצרים (ולא קונים)
        return cart.toArray(new Product[0]);
    }

public Seller[] getSellers() {
        // נשתמש במפה כדי לקבץ את כל השורות של אותו מוכר לאובייקט אחד
        Map<Integer, Seller> sellerMap = new LinkedHashMap<>();
        
        // השאילתה מביאה עכשיו גם את נתוני המוכר וגם את נתוני המוצרים
        String sql = "SELECT u.user_id, u.username, u.password, " +
                     "p.product_id, p.name AS product_name, p.price, p.category, p.is_special_prod " +
                     "FROM Users u " +
                     "LEFT JOIN Products p ON u.user_id = p.seller_id " +
                     "WHERE u.role = 'SELLER' " +
                     "ORDER BY u.user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                
                // 1. אם המוכר הזה עוד לא קיים במפה, ניצור אותו ונוסיף
                sellerMap.putIfAbsent(userId, new Seller(userId, rs.getString("username"), rs.getString("password")));
                
                // 2. נשלוף את המוכר הנוכחי מהמפה
                Seller currentSeller = sellerMap.get(userId);
                
                // 3. נבדוק אם יש מוצר בשורה הזו (בגלל LEFT JOIN, למוכר חדש עשוי להיות NULL)
                if (rs.getString("product_name") != null) {
                    
                    // המרה של המחרוזת מה-DB ל-Enum של הקטגוריה
                    Product.Category category = Product.Category.valueOf(rs.getString("category")); 
                    
                    // יצירת אובייקט המוצר
                    // הערה: אם הוספת בנאי שמקבל גם את ה-ID של המוצר (product_id), מומלץ להכניס אותו פה!
                    Product p = new Product(
                            rs.getInt("product_id"),
                            rs.getString("product_name"), 
                            rs.getFloat("price"), 
                            category, 
                            rs.getBoolean("is_special_prod")
                    );
                    
                    // הוספת המוצר למערך של המוכר (הפונקציה שלך כבר מגדילה את המערך ואת ה-logicSize לבד!)
                    currentSeller.addProduct(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // הופכים את הערכים (המוכרים) שאספנו במפה בחזרה למערך רגיל
        return sellerMap.values().toArray(new Seller[0]);
    }

    public Product[] getProductByCategorie(int category) {
        String categoryStr = "";
        switch (category) {
            case 1: categoryStr = "Children"; break;
            case 2: categoryStr = "Electricity"; break;
            case 3: categoryStr = "Office"; break;
            case 4: categoryStr = "Clothing"; break;
            default:
                System.out.println("Invalid category selected.");
                return new Product[0];
        }
        
        String sql = "SELECT * FROM Products WHERE category = ?::product_category";
        List<Product> productsList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryStr);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    float price = rs.getFloat("price");
                    Product.Category catEnum = Product.Category.valueOf(rs.getString("category"));
                    Product p = new Product(name, price, catEnum, false);
                    productsList.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products by category: " + e.getMessage());
        }
        return productsList.toArray(new Product[0]);
    }

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

    // ==========================================
    // 5. עדכונים (Updates)
    // ==========================================

    public boolean updateSeller(int sellerId, String newUsername, String newPassword) {
        String sql = "UPDATE Users SET username = ?, password = ? WHERE user_id = ? AND role = 'SELLER'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newUsername);
            stmt.setString(2, newPassword);
            stmt.setInt(3, sellerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Error: Username '" + newUsername + "' is already taken.");
            }
            return false;
        }
    }
                
    public boolean updateBuyer(int buyerId, String newUsername, String newPassword) {
        String sql = "UPDATE Users SET username = ?, password = ? WHERE user_id = ? AND role = 'BUYER'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newUsername);
            stmt.setString(2, newPassword);
            stmt.setInt(3, buyerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Error: Username '" + newUsername + "' is already taken.");
            }
            return false;
        }
    }
                
    public boolean updateProductBySeller(int productId, int sellerId, String newName, double newPrice, String newCategory) {
        String sql = "UPDATE Products SET name = ?, price = ?, category = ?::product_category WHERE product_id = ? AND seller_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setDouble(2, newPrice);
            stmt.setString(3, newCategory);
            stmt.setInt(4, productId);
            stmt.setInt(5, sellerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
            
    public boolean updateProductQuantityForBuyer(int buyerId, int productId, int newQuantity) {
        String sql = "UPDATE Cart_Products SET quantity = ? WHERE buyer_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, buyerId);
            stmt.setInt(3, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // 6. תשלום (Payment) ומעבר היסטוריה
    // ==========================================

    public boolean processPayment(int buyerId) {
        // טרנזקציה: יצירת הזמנה חדשה -> העתקת המוצרים מהעגלה להזמנה -> ריקון העגלה הפעילה
        String insertOrderSql = "INSERT INTO Orders (buyer_id, order_time) VALUES (?, CURRENT_TIMESTAMP) RETURNING order_id";
        String moveProductsSql = "INSERT INTO Order_Products (order_id, product_id, quantity) SELECT ?, product_id, quantity FROM Cart_Products WHERE buyer_id = ?";
        String clearCartSql = "DELETE FROM Cart_Products WHERE buyer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); 
            
            try (PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderSql)) {
                insertOrderStmt.setInt(1, buyerId);
                ResultSet rs = insertOrderStmt.executeQuery();
                
                if (rs.next()) {
                    int newOrderId = rs.getInt("order_id");
                    
                    try (PreparedStatement moveProductsStmt = conn.prepareStatement(moveProductsSql)) {
                        moveProductsStmt.setInt(1, newOrderId);
                        moveProductsStmt.setInt(2, buyerId);
                        int itemsMoved = moveProductsStmt.executeUpdate();
                        
                        if (itemsMoved > 0) {
                            try (PreparedStatement clearCartStmt = conn.prepareStatement(clearCartSql)) {
                                clearCartStmt.setInt(1, buyerId);
                                clearCartStmt.executeUpdate();
                            }
                            conn.commit(); 
                            return true;
                        } else {
                            conn.rollback(); // לא היו מוצרים בעגלה
                            return false; 
                        }
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean switchCartWithHistory(int buyerId, int historyOrderId) {
        // טרנזקציה: מחיקת העגלה הפעילה הנוכחית -> העתקת המוצרים מההיסטוריה (Order_Products) חזרה לעגלה הפעילה
        String clearCartSql = "DELETE FROM Cart_Products WHERE buyer_id = ?";
        String copyHistorySql = "INSERT INTO Cart_Products (buyer_id, product_id, quantity) SELECT ?, product_id, quantity FROM Order_Products WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement clearCartStmt = conn.prepareStatement(clearCartSql);
                 PreparedStatement copyStmt = conn.prepareStatement(copyHistorySql)) {
                
                // מנקים את העגלה הנוכחית
                clearCartStmt.setInt(1, buyerId);
                clearCartStmt.executeUpdate();
                
                // מעתיקים מההיסטוריה
                copyStmt.setInt(1, buyerId);
                copyStmt.setInt(2, historyOrderId);
                int itemsCopied = copyStmt.executeUpdate();
                
                conn.commit();
                return itemsCopied > 0;
                
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==========================================
    // 7. מחיקות (Deletions)
    // ==========================================

    public boolean deleteSellerById(int sellerId) {
        String sql = "DELETE FROM Users WHERE user_id = ? AND role = 'SELLER'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sellerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
                
    public boolean deleteBuyerById(int buyerId) {
        String sql = "DELETE FROM Users WHERE user_id = ? AND role = 'BUYER'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, buyerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
                
    public boolean deleteProductBySeller(int productId, int sellerId) {
        String sql = "DELETE FROM Products WHERE product_id = ? AND seller_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, sellerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
                
    public boolean removeProductFromBuyerCart(int buyerId, int productId) {
        String sql = "DELETE FROM Cart_Products WHERE buyer_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, buyerId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // =========================================================================
    // 🏆 דוחות מתקדמים ושאילתות מורכבות - Meaningful Queries 🏆
    // =========================================================================

    // 1. הקונה המגוון (קנה לפחות מוצר אחד מכל קטגוריה - Relational Division)
    public void reportDiverseBuyers() {
        System.out.println("\n--- The Diverse Buyers (Bought from all categories) ---");
        String sql = "SELECT u.username " +
                     "FROM Users u " +
                     "JOIN Orders o ON u.user_id = o.buyer_id " +
                     "JOIN Order_Products op ON o.order_id = op.order_id " +
                     "JOIN Products p ON op.product_id = p.product_id " +
                     "WHERE u.role = 'BUYER' " +
                     "GROUP BY u.user_id, u.username " +
                     "HAVING COUNT(DISTINCT p.category) = 4"; 
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while (rs.next()) {
                System.out.println("🌟 " + rs.getString("username"));
                found = true;
            }
            if (!found) System.out.println("No buyers found who bought from all 4 categories.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 2. לקוחות עצלים (משתמשים רשומים ללא שום פעילות - Set Operations / EXCEPT)
    public void reportLazyBuyers() {
        System.out.println("\n--- Lazy Buyers (Registered but no activity) ---");
        String sql = "SELECT username FROM Users WHERE role = 'BUYER' " +
                     "EXCEPT " +
                     "(SELECT u.username FROM Users u JOIN Orders o ON u.user_id = o.buyer_id " +
                     " UNION " +
                     " SELECT u.username FROM Users u JOIN Cart_Products cp ON u.user_id = cp.buyer_id)";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while (rs.next()) {
                System.out.println("💤 " + rs.getString("username"));
                found = true;
            }
            if (!found) System.out.println("All registered buyers have shown some activity!");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 3. המוכר הרווחי ביותר (Top Seller)
    public void reportTopSeller() {
        System.out.println("\n--- The Most Profitable Seller ---");
        String sql = "SELECT u.username, SUM((p.price + COALESCE(sp.extra_pay, 0)) * op.quantity) as total_revenue " +
                     "FROM Users u " +
                     "JOIN Products p ON u.user_id = p.seller_id " +
                     "JOIN Order_Products op ON p.product_id = op.product_id " +
                     "LEFT JOIN Special_Products sp ON p.product_id = sp.product_id " +
                     "WHERE u.role = 'SELLER' " +
                     "GROUP BY u.user_id, u.username " +
                     "ORDER BY total_revenue DESC LIMIT 1";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("👑 Seller: " + rs.getString("username") + " | Revenue: ₪" + rs.getDouble("total_revenue"));
            } else {
                System.out.println("No sales data available yet.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 4. מוכר עם הקטלוג הכי מגוון
    public void reportMostDiverseSeller() {
        System.out.println("\n--- Seller with the Most Diverse Catalog ---");
        String sql = "SELECT u.username, COUNT(DISTINCT p.category) as categories_covered " +
                     "FROM Users u " +
                     "JOIN Products p ON u.user_id = p.seller_id " +
                     "WHERE u.role = 'SELLER' " +
                     "GROUP BY u.user_id, u.username " +
                     "ORDER BY categories_covered DESC LIMIT 1";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("🎨 Seller: " + rs.getString("username") + " | Categories Covered: " + rs.getInt("categories_covered") + "/4");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 5. מוכרים רדומים (לא נקנה מהם כלום - LEFT JOIN with IS NULL)
    public void reportDormantSellers() {
        System.out.println("\n--- Dormant Sellers (No products sold) ---");
        String sql = "SELECT u.username " +
                     "FROM Users u " +
                     "JOIN Products p ON u.user_id = p.seller_id " +
                     "LEFT JOIN Order_Products op ON p.product_id = op.product_id " +
                     "WHERE u.role = 'SELLER' AND op.product_id IS NULL " +
                     "GROUP BY u.user_id, u.username";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while (rs.next()) {
                System.out.println("😴 " + rs.getString("username"));
                found = true;
            }
            if (!found) System.out.println("All active sellers have sold at least one product.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 6. המוצרים הכי נמכרים
    public void reportBestSellingProducts() {
        System.out.println("\n--- Top 3 Best Selling Products ---");
        String sql = "SELECT p.name, p.category, SUM(op.quantity) as total_sold " +
                     "FROM Products p " +
                     "JOIN Order_Products op ON p.product_id = op.product_id " +
                     "GROUP BY p.product_id, p.name, p.category " +
                     "ORDER BY total_sold DESC LIMIT 3";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int rank = 1;
            while (rs.next()) {
                System.out.println(rank + ". " + rs.getString("name") + " (" + rs.getString("category") + ") - Sold: " + rs.getInt("total_sold") + " units");
                rank++;
            }
            if (rank == 1) System.out.println("No products sold yet.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 7. סך מכירות פר מוכר (Total Sales Per Seller)
    public void reportSalesPerSeller() {
        System.out.println("\n--- Total Sales per Seller ---");
        String sql = "SELECT u.username, COALESCE(SUM((p.price + COALESCE(sp.extra_pay, 0)) * op.quantity), 0) as total_revenue " +
                     "FROM Users u " +
                     "LEFT JOIN Products p ON u.user_id = p.seller_id " +
                     "LEFT JOIN Order_Products op ON p.product_id = op.product_id " +
                     "LEFT JOIN Special_Products sp ON p.product_id = sp.product_id " +
                     "WHERE u.role = 'SELLER' " +
                     "GROUP BY u.user_id, u.username " +
                     "ORDER BY total_revenue DESC";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("👤 " + rs.getString("username") + " | ₪" + rs.getDouble("total_revenue"));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 8. עגלות נטושות לעומת קניות (במקום סטוק נמוך - דורש HAVING ו-COALESCE)
    public void reportAbandonedProducts() {
        System.out.println("\n--- Abandoned Products (More in active carts than sold) ---");
        String sql = "SELECT p.name, COALESCE(SUM(cp.quantity), 0) as in_carts, COALESCE(SUM(op.quantity), 0) as sold " +
                     "FROM Products p " +
                     "LEFT JOIN Cart_Products cp ON p.product_id = cp.product_id " +
                     "LEFT JOIN Order_Products op ON p.product_id = op.product_id " +
                     "GROUP BY p.product_id, p.name " +
                     "HAVING COALESCE(SUM(cp.quantity), 0) > COALESCE(SUM(op.quantity), 0)";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while (rs.next()) {
                System.out.println("🛒 " + rs.getString("name") + " | In Carts: " + rs.getInt("in_carts") + " | Actually Sold: " + rs.getInt("sold"));
                found = true;
            }
            if (!found) System.out.println("No abandoned products found. People are buying what they add!");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 9. התפלגות הכנסות לפי קטגוריה
    public void reportRevenueByCategory() {
        System.out.println("\n--- Revenue Distribution by Category ---");
        String sql = "SELECT p.category, SUM((p.price + COALESCE(sp.extra_pay,0)) * op.quantity) as category_revenue " +
                     "FROM Products p " +
                     "JOIN Order_Products op ON p.product_id = op.product_id " +
                     "LEFT JOIN Special_Products sp ON p.product_id = sp.product_id " +
                     "GROUP BY p.category " +
                     "ORDER BY category_revenue DESC";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while (rs.next()) {
                System.out.println("📦 " + rs.getString("category") + " | ₪" + rs.getDouble("category_revenue"));
                found = true;
            }
            if (!found) System.out.println("No revenue data by category yet.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // 10. הזמנה מעל הממוצע הכללי (Whale Orders - Subquery בתוך HAVING)
    public void reportWhaleOrders() {
        System.out.println("\n--- Whale Orders (Orders above the general average) ---");
        String sql = "SELECT o.order_id, u.username, SUM((p.price + COALESCE(sp.extra_pay,0)) * op.quantity) as order_total " +
                     "FROM Orders o " +
                     "JOIN Users u ON o.buyer_id = u.user_id " +
                     "JOIN Order_Products op ON o.order_id = op.order_id " +
                     "JOIN Products p ON op.product_id = p.product_id " +
                     "LEFT JOIN Special_Products sp ON p.product_id = sp.product_id " +
                     "GROUP BY o.order_id, u.username " +
                     "HAVING SUM((p.price + COALESCE(sp.extra_pay,0)) * op.quantity) > (" +
                     "    SELECT COALESCE(SUM((p2.price + COALESCE(sp2.extra_pay,0)) * op2.quantity) / NULLIF(COUNT(DISTINCT o2.order_id), 0), 0) " +
                     "    FROM Orders o2 " +
                     "    JOIN Order_Products op2 ON o2.order_id = op2.order_id " +
                     "    JOIN Products p2 ON op2.product_id = p2.product_id " +
                     "    LEFT JOIN Special_Products sp2 ON p2.product_id = sp2.product_id" +
                     ") " +
                     "ORDER BY order_total DESC";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while (rs.next()) {
                System.out.println("🐳 Order ID: " + rs.getInt("order_id") + " | Buyer: " + rs.getString("username") + " | Total: ₪" + rs.getDouble("order_total"));
                found = true;
            }
            if (!found) System.out.println("No orders above average found.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void printBuyerCart(int buyerId) {
        String sql = "SELECT p.name, p.price, cp.quantity " +
                     "FROM Cart_Products cp " +
                     "JOIN Products p ON cp.product_id = p.product_id " +
                     "WHERE cp.buyer_id = ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, buyerId); // הכנסת ה-ID של הקונה לשאילתה
            
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasItems = false;
                while (rs.next()) {
                    if (!hasItems) {
                        System.out.println("  🛒 Cart Items:");
                        hasItems = true;
                    }
                    String name = rs.getString("name");
                    float price = rs.getFloat("price");
                    int quantity = rs.getInt("quantity");
                    System.out.println("    - " + quantity + "x " + name + " (" + price + "$ each)");
                }
                if (!hasItems) {
                    System.out.println("  🛒 Cart is empty.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching cart: " + e.getMessage());
        }
    }

    public void printBuyerPaymentHistory(int buyerId) {
        // שאילתה שמביאה את היסטוריית ההזמנות ששולמו, ממוינת מהזמנה חדשה לישנה
        String sql = "SELECT o.order_id, o.order_time, p.name, p.price, op.quantity " +
                     "FROM Orders o " +
                     "JOIN Order_Products op ON o.order_id = op.order_id " +
                     "JOIN Products p ON op.product_id = p.product_id " +
                     "WHERE o.buyer_id = ? " +
                     "ORDER BY o.order_time DESC, o.order_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, buyerId); // הכנסת ה-ID של הקונה לשאילתה
            
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasHistory = false;
                int currentOrderId = -1; // משתנה עזר כדי להדפיס כותרת לכל הזמנה נפרדת

                while (rs.next()) {
                    if (!hasHistory) {
                        System.out.println("  📦 Payment History (Completed Orders):");
                        hasHistory = true;
                    }

                    int orderId = rs.getInt("order_id");
                    
                    // אם זה מספר הזמנה חדש שעוד לא הדפסנו, נדפיס לו כותרת עם תאריך
                    if (orderId != currentOrderId) {
                        System.out.println("    Order #" + orderId + " | Date: " + rs.getTimestamp("order_time"));
                        currentOrderId = orderId;
                    }

                    String name = rs.getString("name");
                    float price = rs.getFloat("price");
                    int quantity = rs.getInt("quantity");
                    System.out.println("      - " + quantity + "x " + name + " (" + price + "$ each)");
                }
                
                if (!hasHistory) {
                    System.out.println("  📦 Payment History is empty (No purchases yet).");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payment history: " + e.getMessage());
        }
    }

    public void removeProductFromCart(int buyerId, int productId) {
        // מוחק רק את השורה שבה הקונה הספציפי מקושר למוצר הספציפי בעגלה
        String sql = "DELETE FROM Cart_Products WHERE buyer_id = ? AND product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, buyerId);
            stmt.setInt(2, productId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product successfully removed from the cart.");
            } else {
                System.out.println("Could not find this product in the cart.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error removing product from cart: " + e.getMessage());
        }
    }

// פונקציה 1: מביאה את כל מספרי ההזמנות (הקבלות) של קונה מסוים
    public int[] getBuyerOrderIds(int buyerId) {
        List<Integer> orderIds = new ArrayList<>();
        String sql = "SELECT order_id FROM Orders WHERE buyer_id = ? ORDER BY order_time DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orderIds.add(rs.getInt("order_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order IDs: " + e.getMessage());
        }
        
        // המרה מ-List למערך int פשוט
        return orderIds.stream().mapToInt(i -> i).toArray();
    }

    // פונקציה 2: מוחקת את העגלה הנוכחית ומעתיקה את המוצרים מההזמנה הישנה
    public void replaceCartWithHistory(int buyerId, int orderId) {
        String clearCartSql = "DELETE FROM Cart_Products WHERE buyer_id = ?";
        // שאילתת SQL קלאסית שמעתיקה נתונים מטבלה אחת לאחרת!
        String copyHistorySql = "INSERT INTO Cart_Products (buyer_id, product_id, quantity) " +
                                "SELECT ?, product_id, quantity FROM Order_Products WHERE order_id = ?";
                                
        try (Connection conn = DatabaseConnection.getConnection()) {
            // שלב א': ריקון העגלה הנוכחית
            try (PreparedStatement clearStmt = conn.prepareStatement(clearCartSql)) {
                clearStmt.setInt(1, buyerId);
                clearStmt.executeUpdate();
            }
            
            // שלב ב': העתקת המוצרים מההזמנה לעגלה
            try (PreparedStatement copyStmt = conn.prepareStatement(copyHistorySql)) {
                copyStmt.setInt(1, buyerId);
                copyStmt.setInt(2, orderId);
                copyStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error replacing cart: " + e.getMessage());
        }
    }

}