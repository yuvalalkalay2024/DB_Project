package DB_Project;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Meaningful_Queries {

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
    System.out.println("\n--- Lazy Buyers (Registered but never ordered) ---");

    String sql = "SELECT u.username " +
                 "FROM Users u " +
                 "WHERE u.role = 'BUYER' " +
                 "AND u.user_id NOT IN ( " +
                 "    SELECT o.buyer_id " +
                 "    FROM Orders o " +
                 ")";

    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        boolean found = false;

        while (rs.next()) {
            System.out.println("Buyer: " + rs.getString("username"));
            found = true;
        }

        if (!found) {
            System.out.println("All buyers have made at least one order.");
        }

    } catch (SQLException e) {
        System.err.println("Error in reportLazyBuyers: " + e.getMessage());
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
 
}
