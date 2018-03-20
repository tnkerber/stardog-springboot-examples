package com.polarisalpha.ca.stardog;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;

@RunWith(Parameterized.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:test.properties")
public class StardogIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();
    @Rule
    public TestName name = new TestName();

    @Parameterized.Parameters
    public static Object[] params() {
        return new Object[] { "/load-n3", "/load-turtle", "/load-rdfxml1", "/load-rdfxml2" };
    }
    @Parameterized.Parameter
    public String endpoint;

    private String dbName;
    private static Stardog stardogEmbeddedServer;

    @BeforeClass
    public static void startStardogEmbeddedServer() {
        stardogEmbeddedServer = Stardog.builder().create();
    }

    @AfterClass
    public static void stopStardogEmbeddedServer() {
        if (stardogEmbeddedServer != null) {
            stardogEmbeddedServer.shutdown();
        }
    }

    @Before
    public void initialize() {
        dbName = name.getMethodName().replaceAll("[\\[\\]]", "_") + System.currentTimeMillis();
    }

    @After
    public void cleanup() {
        final AdminConnection adminConn = AdminConnectionConfiguration
                .toEmbeddedServer()
                .credentials("admin", "admin")
                .connect();
        // drop db
        if (adminConn.list().contains(dbName)) {
            adminConn.drop(dbName);
        }
    }

    @Test
    public void testEndpoint() {
        final String response = restTemplate.getForObject(endpoint + "?dbName=" + dbName, String.class);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.contains(dbName));
    }
}
