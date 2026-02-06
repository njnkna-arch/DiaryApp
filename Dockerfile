# 1. 実行環境（Node.js）を用意
FROM node:20-slim

# 2. アプリの作業ディレクトリを作成
WORKDIR /app

# 3. 依存関係のリスト（package.json）をコピーしてインストール
COPY package*.json ./
RUN npm install

# 4. 残りのすべてのファイルをコピー
COPY . .

# 5. ポート8080を開放
EXPOSE 8080

# 6. アプリを起動するコマンドを実行
CMD ["npm", "start"]
