import io.github.geniot.sayagain.gen.model.RecipeDto;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class IngredientsTest extends TestBase {

    @Test
    public void shouldPostWithIngredients() {
        RecipeDto inRecipeDto = createRecipe();
        inRecipeDto.setIngredients(List.of(createIngredient("potatoes")));
        RecipeDto outRecipeDto = saveRecipe(inRecipeDto);

        assertThat(inRecipeDto).usingRecursiveComparison().ignoringFields("id", "ingredients.id").isEqualTo(outRecipeDto);

        REQUEST.delete("/recipes/" + outRecipeDto.getId()).then().statusCode(HttpStatus.SC_OK);

        //removing a recipe shouldn't remove an ingredient, no cascade delete
        REQUEST.get("/testing/ingredients").then()
                .statusCode(HttpStatus.SC_OK)
                .body("", Matchers.hasSize(1));
    }

    @Test
    public void shouldEditIngredients() {
        RecipeDto inRecipeDto = createRecipe();
        inRecipeDto.setIngredients(List.of(
                createIngredient("potatoes"),
                createIngredient("salt")));

        RecipeDto outRecipeDto = saveRecipe(inRecipeDto);
        assertNotNull(outRecipeDto.getIngredients());
        outRecipeDto.getIngredients().remove(0);

        REQUEST.body(outRecipeDto).put("/recipes");

        RecipeDto outPatchedRecipeDto = REQUEST.get("/recipes/" + outRecipeDto.getId()).as(RecipeDto.class);
        assertNotNull(outPatchedRecipeDto.getIngredients());
        assertEquals(1, outPatchedRecipeDto.getIngredients().size());
    }


}
