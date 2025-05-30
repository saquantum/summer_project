package com.ourrainwater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            String dbName = "rainwaterDB";
            ResultSet rs = stmt.executeQuery("show databases like '" + dbName + "'");
            if (!rs.next()) {
                System.out.println("Initializing database...");
                stmt.executeUpdate("create database " + dbName);
                try (Connection newConn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/" + dbName, "root", "123456")) {

                    Resource resource = new ClassPathResource("sql/init.sql");
                    ScriptUtils.executeSqlScript(newConn, resource);
                    System.out.println("Initialization completed");
                }
            }
        }
    }
}

