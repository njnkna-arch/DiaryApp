package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * データベース接続を管理するクラス。
 * MySQL 9.x (Railway) の最新セキュリティ設定に対応した最終版です。
 */
public class DBConnection {
    public static Connection getConnection() throws Exception {
        // JDBCドライバのロード
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // Railwayの環境変数を取得
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        String url;
        if (host != null) {
            // 【重要】最新のMySQLに対応するため、末尾にセキュリティ許可設定をすべて追加しました
            url = String.format(
                "jdbc:mysql://%s:%s/%s?serverTimezone=JST" +
                "&useUnicode=true&characterEncoding=UTF-8" +
                "&allowPublicKeyRetrieval=true" +
                "&useSSL=false" +
                "&connectionAttributes=program_name:LuminaApp", 
                host, port, dbName
            );
        } else {
            // ローカル（Eclipse）用
            url = "jdbc:mysql://localhost:3306/luminadb?serverTimezone=JST&allowPublicKeyRetrieval=true&useSSL=false";
            user = "root";
            pass = "root"; 
        }

        return DriverManager.getConnection(url, user, pass);
    }
}
