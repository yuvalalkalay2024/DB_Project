package DB_Project;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {

    // 1. CREATE (Insert) - הוספת מוצר חדש
    public boolean insertProduct(int sellerId, String name, double price, String category, boolean isSpecial, Double extraPay) {
        String sqlProduct = "INSERT INTO Products (seller_id, name, price, category, is_special_prod) VALUES (?, ?, ?, ?::product_category, ?) RETURNING product_id";
        String sqlSpecial = "INSERT INTO Special_Products (product_id, extra_pay) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // שימוש בטרנזקציה למקרה של מוצר מיוחד

            try (PreparedStatement stmtProd = conn.prepareStatement(sqlProduct)) {
                stmtProd.setInt(1, sellerId);
                stmtProd.setString(2, name);
                stmtProd.setDouble(3, price);
                stmtProd.setString(4, category);
                stmtProd.setBoolean(5, isSpecial);
                
                ResultSet rs = stmtProd.executeQuery();
                if (rs.next()) {
                    int productId = rs.getInt("product_id");

                    // אם זה מוצר מיוחד, נכניס גם לטבלת הבת
                    if (isSpecial && extraPay != null) {
                        try (PreparedStatement stmtSpec = conn.prepareStatement(sqlSpecial)) {
                            stmtSpec.setInt(1, productId);
                            stmtSpec.setDouble(2, extraPay);
                            stmtSpec.executeUpdate();
                        }
                    }
                }
                conn.commit(); // אישור השינויים
                return true;
            } catch (SQLException e) {
                conn.rollback(); // ביטול הכל במקרה של שגיאה
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. READ (Search) - חיפוש מוצרים לפי שם
    public void searchProductsByName(String keyword) {
        String sql = "SELECT p.*, s.extra_pay FROM Products p LEFT JOIN Special_Products s ON p.product_id = s.product_id WHERE p.name ILIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%"); // שימוש ב-LIKE לחיפוש גמיש
            ResultSet rs = stmt.executeQuery();

            System.out.println("--- Search Results ---");
            while (rs.next()) {
                int id = rs.getInt("product_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String category = rs.getString("category");
                boolean isSpecial = rs.getBoolean("is_special_prod");
                
                System.out.print("ID: " + id + " | Name: " + name + " | Price: ₪" + price + " | Category: " + category);
                if (isSpecial) {
                    System.out.println(" | [Special Product] Extra Pay: ₪" + rs.getDouble("extra_pay"));
                } else {
                    System.out.println();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. UPDATE - עדכון מחיר של מוצר
    public boolean updateProductPrice(int productId, double newPrice) {
        String sql = "UPDATE Products SET price = ? WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, newPrice);
            stmt.setInt(2, productId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. DELETE - מחיקת מוצר
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM Products WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // יחזיר אמת אם המוצר אכן היה קיים ונמחק
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}