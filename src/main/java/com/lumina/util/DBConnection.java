package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * データベース接続管理。
 * Railway環境変数を読み取り、MySQL 9.x系に最適化された接続を行います。
 */
public class DBConnection {
    public static Connection getConnection() throws Exception {
        // 1. ドライバのロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new Exception("MySQLドライバが見つかりません。pom.xmlの設定を確認してください。");
        }
        
        // 2. 環境変数の取得
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        // クラウド環境（Railway）での接続URL組み立て
        if (host != null && !host.isEmpty()) {
            
            // エラー診断: もし変数が一つでも null なら具体的にエラーを投げる
            // これにより、ブラウザの「null」表示を具体的なメッセージに変えます
            if (user == null) throw new Exception("設定エラー: MYSQLUSER が設定されていません。");
            if (pass == null) throw new Exception("設定エラー: MYSQLPASSWORD が設定されていません。");
            if (dbName == null) throw new Exception("設定エラー: MYSQLDATABASE が設定されていません。");

            // 内部接続(railway.internal)ならポート3306を強制使用
            String finalPort = host.contains("railway.internal") ? "3306" : port;
            String url = String.format("jdbc:mysql://%s:%s/%s", host, finalPort, dbName);

            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", pass);
            props.setProperty("serverTimezone", "JST");
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");
            props.setProperty("allowPublicKeyRetrieval", "true");
            props.setProperty("useSSL", "false");
            props.setProperty("connectTimeout", "10000"); // 10秒待機

            System.out.println("🚀 [DB接続] 試行中: " + url + " (User: " + user + ")");
            
            try {
                return DriverManager.getConnection(url, props);
            } catch (Exception e) {
                throw new Exception("データベース接続に失敗しました。パスワードが正しいか再確認してください: " + e.getMessage());
            }
        } else {
            // ローカル（Eclipse）環境
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/luminadb?serverTimezone=JST&allowPublicKeyRetrieval=true&useSSL=false", "root", "root");
        }
    }
}
