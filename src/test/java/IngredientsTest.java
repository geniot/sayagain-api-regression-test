import io.github.geniot.sayagain.gen.model.IngredientDto;
import io.github.geniot.sayagain.gen.model.RecipeDto;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IngredientsTest extends TestBase {

    @Test
    public void shouldPostWithIngredients() {
        RecipeDto inRecipeDto = createRecipe();
        IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setName("potatoes");
        inRecipeDto.setIngredients(List.of(ingredientDto));

        //post
        Response createRecipeResponse = REQUEST.body(inRecipeDto).post("/recipes");
        createRecipeResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
        RecipeDto outRecipeDto = createRecipeResponse.as(RecipeDto.class);

        assertThat(inRecipeDto).usingRecursiveComparison().ignoringFields("id", "ingredients.id").isEqualTo(outRecipeDto);

        REQUEST.delete("/recipes/" + outRecipeDto.getId()).then().statusCode(HttpStatus.SC_OK);

        //removing a recipe shouldn't remove an ingredient, no cascade delete
        REQUEST.get("/ingredients").then()
                .statusCode(HttpStatus.SC_OK)
                .body("", Matchers.hasSize(1));
    }
}
