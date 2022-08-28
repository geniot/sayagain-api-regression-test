import io.github.geniot.sayagain.gen.model.RecipeDto;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RecipesTest extends TestBase {

    Random random = new Random(System.currentTimeMillis());

    @Before
    public void setup() {
        REQUEST.delete("/recipes").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void shouldHaveStatus200ForAllRecipesList() {
        REQUEST.get("/recipes").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void shouldHaveEmptyRecipesList() {
        REQUEST.get("/recipes").then()
                .statusCode(HttpStatus.SC_OK)
                .body("", Matchers.hasSize(0));
    }

    @Test
    public void shouldPutDelete() {
        RecipeDto inRecipeDto = new RecipeDto();
        inRecipeDto.setTitle(UUID.randomUUID().toString());
        inRecipeDto.setDescription(UUID.randomUUID().toString());
        inRecipeDto.setVegetarian(random.nextBoolean());
        inRecipeDto.setServings(random.nextInt());

        Response createRecipeResponse = REQUEST.body(inRecipeDto).post("/recipes");
        createRecipeResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
        RecipeDto outRecipeDto = createRecipeResponse.as(RecipeDto.class);

        assertThat(inRecipeDto).usingRecursiveComparison().ignoringFields("id").isEqualTo(outRecipeDto);

        REQUEST.get("/recipes").then().statusCode(200).body("", Matchers.hasSize(1));


    }


}
