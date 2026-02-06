const express = require('express');
const mysql = require('mysql2/promise');
const crypto = require('crypto');
require('dotenv').config();

const app = express();
app.use(express.json({ limit: '20mb' })); // 写真などの大容量データ対応
app.use(express.urlencoded({ extended: true, limit: '20mb' }));

app.use(express.static('public'));

const pool = mysql.createPool({
    host: process.env.MYSQLHOST,
    user: process.env.MYSQLUSER,
    password: process.env.MYSQLPASSWORD,
    database: process.env.MYSQLDATABASE,
    port: process.env.MYSQLPORT || 3306,
    ssl: { rejectUnauthorized: false },
    waitForConnections: true,
    connectionLimit: 10
});

// 1. 庭園一覧
app.get('/api/list', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT group_id, group_name, host_name, updated_at FROM DIARY_GROUPS ORDER BY updated_at DESC');
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 2. 新規庭園作成
app.post('/api/create', async (req, res) => {
    try {
        const { name, host, pass } = req.body;
        const id = crypto.randomUUID().substring(0, 8).toUpperCase();
        await pool.query(
            'INSERT INTO DIARY_GROUPS (group_id, group_name, host_name, password) VALUES (?, ?, ?, ?)',
            [id, name, host, pass]
        );
        res.json({ success: true, id });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 3. 投稿取得
app.get('/api/entries', async (req, res) => {
    try {
        const { groupId } = req.query;
        const [rows] = await pool.query('SELECT * FROM DIARY_ENTRIES WHERE group_id = ? ORDER BY created_at ASC', [groupId]);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 4. 投稿追加
app.post('/api/addEntry', async (req, res) => {
    try {
        const { groupId, date, message, photo, color } = req.body;
        await pool.query(
            'INSERT INTO DIARY_ENTRIES (group_id, diary_date, message, image_data, color) VALUES (?, ?, ?, ?, ?)',
            [groupId, date, message, photo, color]
        );
        await pool.query('UPDATE DIARY_GROUPS SET updated_at = NOW() WHERE group_id = ?', [groupId]);
        res.json({ success: true });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 5. 投稿削除 (追加)
app.post('/api/deleteEntry', async (req, res) => {
    try {
        const { entryId } = req.body;
        await pool.query('DELETE FROM DIARY_ENTRIES WHERE id = ?', [entryId]);
        res.json({ success: true });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 6. 閉園（庭園削除） (追加)
app.post('/api/closeGarden', async (req, res) => {
    try {
        const { groupId } = req.body;
        // 関連する投稿を全削除
        await pool.query('DELETE FROM DIARY_ENTRIES WHERE group_id = ?', [groupId]);
        // 庭園自体を削除
        await pool.query('DELETE FROM DIARY_GROUPS WHERE group_id = ?', [groupId]);
        res.json({ success: true });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`--- LUMINA Server started on ${PORT} ---`);
});
