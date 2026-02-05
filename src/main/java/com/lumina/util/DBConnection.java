package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // Railway環境変数を自動取得
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        if (host == null) {
            // ローカル環境用（念のため）
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/luminadb?serverTimezone=JST", "root", "root");
        }

        String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=JST&useUnicode=true&characterEncoding=UTF-8", 
                                    host, port, dbName);
        return DriverManager.getConnection(url, user, pass);
    }
}
