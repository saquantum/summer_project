package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.controller.filter.RequestBlockerFilter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class TestController {

    @Autowired
    private DataSource dataSource;

    private void terminateOtherConnections() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                    "select pg_terminate_backend(pg_stat_activity.pid) " +
                            "from pg_stat_activity " +
                            "where datname = current_database() and pid <> pg_backend_pid();"
            );
        }
    }

    private final MockDataInitializer mockDataInitializer;

    public TestController(MockDataInitializer mockDataInitializer) {
        this.mockDataInitializer = mockDataInitializer;
    }

    @PostMapping("/test/reset")
    public ResponseBody resetDatabase() {
        try {
            RequestBlockerFilter.BLOCKING_REQUESTS = true;
            terminateOtherConnections();
            mockDataInitializer.forceReload();
            return new ResponseBody(Code.SUCCESS, "Reset successful");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBody(Code.SYSTEM_ERR, "Reset failed: " + e.getMessage());
        } finally {
            RequestBlockerFilter.BLOCKING_REQUESTS = false;
        }
    }
}
