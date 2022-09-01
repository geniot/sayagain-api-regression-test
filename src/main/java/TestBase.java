import io.github.geniot.sayagain.gen.model.IngredientDto;
import io.github.geniot.sayagain.gen.model.RecipeDto;
import io.github.geniot.sayagain.gen.model.SearchCriteriaDto;
import io.github.geniot.sayagain.gen.model.UserDto;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Before;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TestBase {
    public Header BEARER_1;
    public Header BEARER_2;

    Random random = new Random(System.currentTimeMillis());

    @Before
    public void setup() {
        request().delete("/testing/recipes").then().statusCode(HttpStatus.SC_OK);
        request().delete("/testing/ingredients").then().statusCode(HttpStatus.SC_OK);
        request().delete("/testing/users").then().statusCode(HttpStatus.SC_OK);

        BEARER_1 = new Header("Authorization", "Bearer " + signIn("test@test.com", "Somepass-2"));
        BEARER_2 = new Header("Authorization", "Bearer " + signIn("test2@test2.com", "Somepass-3"));
    }

    private String signIn(String email, String password) {
        //signup
        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        userDto.setPassword(password);
        Response signUpResponse = request().body(userDto).post("/users/signup");
        signUpResponse.then().statusCode(HttpStatus.SC_OK);

        //signin
        Response signInResponse = request().body(userDto).post("/users/signin");
        signInResponse.then().statusCode(HttpStatus.SC_OK);
        return signInResponse.asString();//or response.then().extract().body().asString();
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
            RestAssured.useRelaxedHTTPSValidation();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public RequestSpecification request() {
        return RestAssured.given().contentType(ContentType.JSON);
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

    RecipeDto saveRecipe(Header bearer, RecipeDto inRecipeDto) {
        Response createRecipeResponse = request().header(bearer).body(inRecipeDto).post("/recipes");
        createRecipeResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
        return createRecipeResponse.as(RecipeDto.class);
    }

    RecipeDto loadRecipe(Integer id) {
        Response getRecipeResponse = request().header(BEARER_1).get("/recipes/" + id);
        getRecipeResponse.then().assertThat().statusCode(HttpStatus.SC_OK);
        return getRecipeResponse.as(RecipeDto.class);
    }

    void search(Header bearer, SearchCriteriaDto searchCriteriaDto, int expectedSize) {
        Response searchResponse = request().header(bearer).body(searchCriteriaDto).post("/recipes/search");
        List<RecipeDto> outRecipesListDto = searchResponse.body().jsonPath().getList("", RecipeDto.class);
        assertEquals(expectedSize, outRecipesListDto.size());
    }

}
