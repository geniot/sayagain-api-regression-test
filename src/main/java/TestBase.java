import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.util.Properties;

public class TestBase {
    public RequestSpecification REQUEST;

    public TestBase() {
        try {
            String env = System.getProperty("env") == null ? "dev" : System.getProperty("env");
            Properties props = new Properties();
            props.load(getClass().getClassLoader().getResourceAsStream("config-" + env + ".properties"));

            //Rest Assured config
            RestAssured.baseURI = props.getProperty("api.uri");
            RestAssured.port = Integer.valueOf(props.getProperty("api.port"));
            RestAssured.basePath = props.getProperty("api.path");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //basic request setting
        REQUEST = RestAssured.given().contentType(ContentType.JSON);
    }
}
