package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * データベース接続管理。
 * Railwayの環境変数を一切介さず、ソースコードに直接情報を書き込むことで
 * 設定ミスを物理的に排除したデバッグ用・最短接続構成です。
 */
public class DBConnection {
    public static Connection getConnection() throws Exception {
        // 1. ドライバのロード
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // 2. 接続情報を直接指定（ここを書き換えるだけでOK）
        // Railway内部ネットワーク用の情報をそのまま打ち込みます
        String host = "mysql.railway.internal";
        String port = "51097";
        String db   = "railway";
        String user = "hironaka";
        
        // あなたのMySQLのVariablesに表示されている「本物のパスワード」をここに直書きしてください
        String pass = "2Tooput5"; 

        // 3. 接続URLの組み立て
        // 内部接続なので、ポートは必ず 3306 です。
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db + 
                     "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=JST";

        // 4. 接続して返す
        // 万が一失敗した場合は、理由（パスワード間違い等）を例外として投げます
        return DriverManager.getConnection(url, user, pass);
    }
}
