// package database;
package DB_Project;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // הגדרת פרטי החיבור ל-PostgreSQL ב-Ubuntu שלך
    private static final String URL = "jdbc:postgresql://localhost:5432/DB_Project";
    private static final String USER = "postgres";
    private static final String PASSWORD = "yuval123"; // החלף בסיסמה שלך

    /**
     * מתודה להשגת חיבור פעיל למסד הנתונים.
     * @return Connection object
     * @throws SQLException אם החיבור נכשל
     */
    public static Connection getConnection() throws SQLException {
        try {
            // טעינת הדרייבר של PostgreSQL (אופציונלי בגרסאות ג'אווה חדישות, אך מומלץ)
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found", e);
        }
    }
}