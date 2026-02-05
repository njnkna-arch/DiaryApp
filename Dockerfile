# --- ステージ1: ビルド（コンパイル） ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# --- ステージ2: 実行（Tomcat） ---
FROM tomcat:10.1-jdk21
# ステージ1で作られた ROOT.war だけを取り出してTomcatに配置
COPY --from=build target/ROOT.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]
