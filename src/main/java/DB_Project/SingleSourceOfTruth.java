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
    // 2. הוספת מוצרים (לקטלוג או לעגלה)
    // ==========================================

    public boolean addProductToSeller(int sellerId, Product p) {
        String sql = "INSERT INTO Products (seller_id, name, price, category, is_special_prod) VALUES (?, ?, ?, ?::product_category, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sellerId);
            stmt.setString(2, p.getName());
            stmt.setDouble(3, p.getPrice());
            stmt.setString(4, p.getCategory().name());
            stmt.setBoolean(5, p.getisSpecialProd());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding product to seller: " + e.getMessage());
            return false;
        }
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
}