package com.lumina.util;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        String url;
        if (host != null) {
            // 末尾にセキュリティ許可設定を追加
            url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=JST&useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false", host, port, dbName);
        } else {
            url = "jdbc:mysql://localhost:3306/luminadb?serverTimezone=JST&useUnicode=yes&characterEncoding=UTF-8";
            user = "root"; pass = "root";
        }
        return DriverManager.getConnection(url, user, pass);
    }
}
