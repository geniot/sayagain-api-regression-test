import io.github.geniot.sayagain.gen.model.IngredientDto;
import io.github.geniot.sayagain.gen.model.RecipeDto;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Before;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class TestBase {
    public RequestSpecification REQUEST;

    Random random = new Random(System.currentTimeMillis());

    @Before
    public void setup() {
        REQUEST.delete("/recipes").then().statusCode(HttpStatus.SC_OK);
        REQUEST.delete("/ingredients").then().statusCode(HttpStatus.SC_OK);
    }

    public TestBase() {
        try {
            String env = System.getProperty("env") == null ? "dev" : System.getProperty("env");
            Properties props = new Properties();
            props.load(getClass().getClassLoader().getResourceAsStream("config-" + env + ".properties"));

            //Rest Assured config
            RestAssured.baseURI = props.getProperty("api.uri");
            if (!StringUtils.isEmpty(props.getProperty("api.port"))) {
                RestAssured.port = Integer.parseInt(props.getProperty("api.port"));
            }
            RestAssured.basePath = props.getProperty("api.path");
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //basic request setting
        REQUEST = RestAssured.given().contentType(ContentType.JSON);
    }

    RecipeDto createRecipe() {
        RecipeDto inRecipeDto = new RecipeDto();
        inRecipeDto.setTitle(UUID.randomUUID().toString());
        inRecipeDto.setDescription(UUID.randomUUID().toString());
        inRecipeDto.setVegetarian(random.nextBoolean());
        inRecipeDto.setServings(random.nextInt());
        return inRecipeDto;
    }

    IngredientDto createIngredient(String name) {
        IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setName(name);
        return ingredientDto;
    }

    RecipeDto saveRecipe(RecipeDto inRecipeDto) {
        Response createRecipeResponse = REQUEST.body(inRecipeDto).post("/recipes");
        createRecipeResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
        return createRecipeResponse.as(RecipeDto.class);
    }
}
