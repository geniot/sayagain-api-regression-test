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
    public void shouldPostGetPutDelete() {
        RecipeDto inRecipeDto = createRecipe();
        //post
        RecipeDto outRecipeDto = saveRecipe(inRecipeDto);

        assertThat(inRecipeDto).usingRecursiveComparison().ignoringFields("id").isEqualTo(outRecipeDto);
        assertNotNull(outRecipeDto.getId());
        REQUEST.get("/recipes").then().statusCode(HttpStatus.SC_OK).body("", Matchers.hasSize(1));

        //get
        RecipeDto outGetRecipeDto = loadRecipe(outRecipeDto.getId());
        assertThat(outRecipeDto).usingRecursiveComparison().ignoringFields("ingredients").isEqualTo(outGetRecipeDto);
        //todo: fix empty vs null
        assertTrue(outRecipeDto.getIngredients() == null || outRecipeDto.getIngredients().isEmpty());
        assertTrue(outGetRecipeDto.getIngredients() == null || outGetRecipeDto.getIngredients().isEmpty());

        //put
        outRecipeDto.setTitle(null);
        REQUEST.body(outRecipeDto).put("/recipes");

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
    public void shouldSearchForRecipesVegetarian() {
        RecipeDto inRecipeDto = createRecipe();
        inRecipeDto.setVegetarian(false);
        RecipeDto outRecipeDto = REQUEST.body(inRecipeDto).post("/recipes").as(RecipeDto.class);

        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        searchCriteriaDto.setVegetarian(true);

        search(searchCriteriaDto, 0);

        outRecipeDto.setVegetarian(true);
        REQUEST.body(outRecipeDto).put("/recipes");

        search(searchCriteriaDto, 1);

        searchCriteriaDto.setVegetarian(null);
        search(searchCriteriaDto, 1);

    }

    @Test
    public void shouldSearchForRecipesServings() {
        RecipeDto inRecipeDto = createRecipe();
        inRecipeDto.setServings(3);
        RecipeDto outRecipeDto = REQUEST.body(inRecipeDto).post("/recipes").as(RecipeDto.class);

        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        searchCriteriaDto.setServings(2);

        search(searchCriteriaDto, 0);

        outRecipeDto.setServings(2);
        REQUEST.body(outRecipeDto).put("/recipes");

        search(searchCriteriaDto, 1);

        searchCriteriaDto.setServings(null);
        search(searchCriteriaDto, 1);

    }

    @Test
    public void shouldUseAnd() {
        RecipeDto inRecipeDto = createRecipe();
        inRecipeDto.setServings(3);
        inRecipeDto.setVegetarian(false);
        RecipeDto outRecipeDto = REQUEST.body(inRecipeDto).post("/recipes").as(RecipeDto.class);

        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        searchCriteriaDto.setServings(3);
        searchCriteriaDto.setVegetarian(true);

        search(searchCriteriaDto, 0);

        outRecipeDto.setVegetarian(true);
        REQUEST.body(outRecipeDto).put("/recipes");

        search(searchCriteriaDto, 1);

        searchCriteriaDto.setVegetarian(null);
        searchCriteriaDto.setServings(null);
        search(searchCriteriaDto, 1);

    }

    @Test
    public void shouldSearchByIngredients() {
        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        RecipeDto inRecipeDto = createRecipe();

        inRecipeDto.setIngredients(List.of(
                createIngredient("potatoes"),
                createIngredient("salt")));

        REQUEST.body(inRecipeDto).post("/recipes").as(RecipeDto.class);

        searchCriteriaDto.setIncludeIngredients(List.of(createIngredient("sugar")));
        search(searchCriteriaDto, 0);

        searchCriteriaDto.setIncludeIngredients(List.of(createIngredient("salt")));
        search(searchCriteriaDto, 1);

        searchCriteriaDto.setIncludeIngredients(null);
        searchCriteriaDto.setExcludeIngredients(List.of(createIngredient("salt")));
        search(searchCriteriaDto, 0);
    }

    @Test
    public void shouldSearchByKeywords() {
        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();

        RecipeDto inRecipeDto = createRecipe();
        inRecipeDto.setDescription("some interesting recipe");
        REQUEST.body(inRecipeDto).post("/recipes").as(RecipeDto.class);

        searchCriteriaDto.setKeyword("any");
        search(searchCriteriaDto, 0);

        searchCriteriaDto.setKeyword("interest");
        search(searchCriteriaDto, 1);
    }

    private void search(SearchCriteriaDto searchCriteriaDto, int expectedSize) {
        Response searchResponse = REQUEST.body(searchCriteriaDto).post("/recipes/search");
        List<RecipeDto> outRecipesListDto = searchResponse.body().jsonPath().getList("", RecipeDto.class);
        assertEquals(expectedSize, outRecipesListDto.size());
    }


}
