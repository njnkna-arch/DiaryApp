package makeupdays.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * クラウド(Railway)とローカル両方で動作するように調整したサーブレット
 */
@WebServlet("/DiaryServlet")
public class DiaryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"; 

    private Connection getConnection() throws Exception {
        Class.forName(JDBC_DRIVER);

        // Railwayが提供する環境変数を取得（設定されていない場合はnullになる）
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        String dbUrl;
        if (host != null) {
            // クラウド環境（Railway）用の接続文字列
            dbUrl = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=JST", host, port, dbName);
        } else {
            // ローカル環境用の接続文字列
            dbUrl = "jdbc:mysql://localhost:3306/mycoorddb?serverTimezone=JST";
            user = "root"; 
            pass = "あなたのローカルのパスワード"; 
        }

        return DriverManager.getConnection(dbUrl, user, pass);
    }

    // --- doGet, doPost メソッドなどは以前の MariaDB 用コードと同じものを使用してください ---
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        String dateStr = request.getParameter("date");
        
        if (dateStr == null || dateStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"日付が必要です\"}");
            return;
        }

        Map<String, Object> diaryData = new HashMap<>();
        try (Connection conn = getConnection()) {
            // DIARY取得
            String sqlDiary = "SELECT entry_text FROM DIARY WHERE diary_date = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDiary)) {
                stmt.setString(1, dateStr);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        diaryData.put("date", dateStr);
                        diaryData.put("text", rs.getString("entry_text"));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                }
            }
            // PHOTO取得
            String sqlPhoto = "SELECT file_path FROM PHOTO WHERE diary_date = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlPhoto)) {
                stmt.setString(1, dateStr);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) diaryData.put("photo", rs.getString("file_path"));
                }
            }
            // JSON出力
            out.print(buildJson(diaryData));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    private String buildJson(Map<String, Object> data) {
        // 簡易JSON構築ロジック（前述のものと同じ）
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        if (sb.length() > 1) sb.setLength(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }
}