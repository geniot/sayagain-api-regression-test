import io.github.geniot.sayagain.gen.model.RecipeDto;
import io.github.geniot.sayagain.gen.model.SearchCriteriaDto;
import org.junit.Test;

public class UsersTest extends TestBase {

    @Test
    public void shouldKeepRecipesSeparate() {
        RecipeDto inRecipeDto = createRecipe();
        inRecipeDto.setVegetarian(true);

        request().header(BEARER_1).body(inRecipeDto).post("/recipes").as(RecipeDto.class);
        request().header(BEARER_2).body(inRecipeDto).post("/recipes").as(RecipeDto.class);

        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        searchCriteriaDto.setVegetarian(true);

        search(BEARER_1, searchCriteriaDto, 1);
        search(BEARER_2, searchCriteriaDto, 1);
    }
}
