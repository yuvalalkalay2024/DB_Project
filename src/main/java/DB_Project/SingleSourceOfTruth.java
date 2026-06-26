package DB_Project;
import java.sql.*;
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
}