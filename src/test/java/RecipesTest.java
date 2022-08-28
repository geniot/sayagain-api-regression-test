import io.github.geniot.sayagain.gen.model.RecipeDto;
import io.github.geniot.sayagain.gen.model.SearchCriteriaDto;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class RecipesTest extends TestBase {

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
    public void shouldPostPatchDelete() {
        RecipeDto inRecipeDto = createRecipe();

        //post
        Response createRecipeResponse = REQUEST.body(inRecipeDto).post("/recipes");
        createRecipeResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
        RecipeDto outRecipeDto = createRecipeResponse.as(RecipeDto.class);

        assertThat(inRecipeDto).usingRecursiveComparison().ignoringFields("id").isEqualTo(outRecipeDto);
        assertNotNull(outRecipeDto.getId());
        REQUEST.get("/recipes").then().statusCode(HttpStatus.SC_OK).body("", Matchers.hasSize(1));

        //patch
        outRecipeDto.setTitle(null);
        REQUEST.body(outRecipeDto).patch("/recipes");
        RecipeDto outPatchedRecipeDto = REQUEST.get("/recipes/" + outRecipeDto.getId()).as(RecipeDto.class);
        assertNull(outPatchedRecipeDto.getTitle());

        //delete
        REQUEST.delete("/recipes/" + outRecipeDto.getId()).then().statusCode(HttpStatus.SC_OK);
        REQUEST.get("/recipes").then().statusCode(HttpStatus.SC_OK).body("", Matchers.hasSize(0));
    }

    @Test
    public void shouldPostBadRequest() {
        RecipeDto inRecipeDto = new RecipeDto();
        inRecipeDto.setId(1);
        REQUEST.body(inRecipeDto).post("/recipes").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void shouldSearchForRecipes() {
        RecipeDto inRecipeDto = createRecipe();
        inRecipeDto.setVegetarian(false);
        RecipeDto outRecipeDto = REQUEST.body(inRecipeDto).post("/recipes").as(RecipeDto.class);

        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        searchCriteriaDto.setVegetarian(true);

        search(searchCriteriaDto, 0);

        outRecipeDto.setVegetarian(true);
        REQUEST.body(outRecipeDto).patch("/recipes");

        search(searchCriteriaDto, 1);

        searchCriteriaDto.setVegetarian(null);
        search(searchCriteriaDto, 1);

    }

    private void search(SearchCriteriaDto searchCriteriaDto, int expectedSize) {
        Response searchResponse = REQUEST.body(searchCriteriaDto).post("/recipes/search");
        List<RecipeDto> outRecipesListDto = searchResponse.body().jsonPath().getList("", RecipeDto.class);
        assertEquals(expectedSize, outRecipesListDto.size());
    }



}
