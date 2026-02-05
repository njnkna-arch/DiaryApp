package com.lumina.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.lumina.util.DBConnection;

/**
 * LUMINAメインサーブレット。
 * フロントエンド（index.html）からのリクエストを受け取り、
 * 日記の作成、一覧取得、投稿の保存を行います。
 */
@WebServlet("/LuminaServlet")
public class LuminaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try (Connection conn = DBConnection.getConnection()) {
            if ("list".equals(action)) {
                // 庭園（グループ）の一覧を取得
                String sql = "SELECT group_id, group_name, host_name, updated_at FROM DIARY_GROUPS ORDER BY updated_at DESC";
                try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                    out.print(resultSetToJson(rs));
                }
            } else if ("getEntries".equals(action)) {
                // 特定の庭園内の全投稿を取得
                String gid = request.getParameter("groupId");
                String sql = "SELECT * FROM DIARY_ENTRIES WHERE group_id = ? ORDER BY diary_date ASC, created_at ASC";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, gid);
                    try (ResultSet rs = stmt.executeQuery()) {
                        out.print(resultSetToJson(rs));
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try (Connection conn = DBConnection.getConnection()) {
            if ("create".equals(action)) {
                // 新規庭園作成
                String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String sql = "INSERT INTO DIARY_GROUPS (group_id, group_name, host_name, password) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, id);
                    stmt.setString(2, request.getParameter("name"));
                    stmt.setString(3, request.getParameter("host"));
                    stmt.setString(4, request.getParameter("pass"));
                    stmt.executeUpdate();
                    out.print("{\"success\":true, \"id\":\"" + id + "\"}");
                }
            } else if ("addEntry".equals(action)) {
                // 新規投稿（メッセージと画像）の保存
                String sql = "INSERT INTO DIARY_ENTRIES (group_id, diary_date, message, image_data, color) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, request.getParameter("groupId"));
                    stmt.setString(2, request.getParameter("date"));
                    stmt.setString(3, request.getParameter("message"));
                    stmt.setString(4, request.getParameter("photo"));
                    stmt.setString(5, request.getParameter("color"));
                    stmt.executeUpdate();
                }
                // 最終更新日時を更新
                String updateSql = "UPDATE DIARY_GROUPS SET updated_at = NOW() WHERE group_id = ?";
                try (PreparedStatement upStmt = conn.prepareStatement(updateSql)) {
                    upStmt.setString(1, request.getParameter("groupId"));
                    upStmt.executeUpdate();
                }
                out.print("{\"success\":true}");
            }
        } catch (Exception e) {
            response.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String resultSetToJson(ResultSet rs) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        boolean first = true;
        while (rs.next()) {
            if (!first) sb.append(",");
            sb.append("{");
            for (int i = 1; i <= cols; i++) {
                String key = md.getColumnLabel(i);
                Object val = rs.getObject(i);
                String strVal = (val == null) ? "" : String.valueOf(val).replace("\"", "\\\"").replace("\n", " ");
                sb.append("\"").append(key).append("\":\"").append(strVal).append("\"");
                if (i < cols) sb.append(",");
            }
            sb.append("}");
            first = false;
        }
        return sb.append("]").toString();
    }
}
