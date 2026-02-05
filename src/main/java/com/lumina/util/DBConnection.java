package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * データベース接続管理。
 * RailwayのMySQL 9.x系で発生する接続エラーを完全に回避するための最終設定です。
 */
public class DBConnection {
    public static Connection getConnection() throws Exception {
        // 1. ドライバのロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new Exception("MySQLドライバが見つかりません。pom.xmlの設定を確認してください。");
        }
        
        // 2. Railway環境変数の取得
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        // 3. 接続設定の構築
        if (host != null && !host.isEmpty()) {
            // 内部接続(railway.internal)ならポート3306を強制使用
            String finalPort = host.contains("railway.internal") ? "3306" : port;
            String url = String.format("jdbc:mysql://%s:%s/%s", host, finalPort, dbName);

            // MySQL 9.4の厳しいセキュリティ設定を突破するためのプロパティ
            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", pass);
            props.setProperty("serverTimezone", "JST");
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");
            
            // 【最重要】最新MySQLでこれがないとパスワード送信でエラーになります
            props.setProperty("allowPublicKeyRetrieval", "true"); 
            props.setProperty("useSSL", "false"); 
            
            // 接続維持とタイムアウト設定
            props.setProperty("connectTimeout", "15000"); // 15秒待機
            props.setProperty("socketTimeout", "30000");  // 30秒待機

            System.out.println("🔍 [DB接続] 内部ネットワーク経由で接続を試みます: " + host);
            
            try {
                return DriverManager.getConnection(url, props);
            } catch (Exception e) {
                // エラー内容を日本語でわかりやすく表示
                String msg = e.getMessage();
                if (msg.contains("Access denied")) {
                    throw new Exception("【エラー】パスワードまたはユーザー名が違います。RailwayのVariablesを確認してください。");
                } else if (msg.contains("Communications link failure")) {
                    throw new Exception("【エラー】データベースへの通信がタイムアウトしました。もう一度リロードしてください。");
                }
                throw new Exception("データベース接続失敗: " + msg);
            }
        } else {
            // ローカル（Eclipse）環境用
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/luminadb?serverTimezone=JST&allowPublicKeyRetrieval=true&useSSL=false", "root", "root");
        }
    }
}
