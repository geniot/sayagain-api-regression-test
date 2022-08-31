import io.github.geniot.sayagain.gen.model.RecipeDto;
import io.github.geniot.sayagain.gen.model.SearchCriteriaDto;
import io.github.geniot.sayagain.gen.model.UserDto;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void shouldValidateCredentials() {
        UserDto userDto = new UserDto();
        userDto.setEmail("wrong email");
        userDto.setPassword(null);
        Response signUpResponse = request().body(userDto).post("/users/signup");
        signUpResponse.then().statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
        String validationErrors = signUpResponse.jsonPath().get("message");
        assertEquals(validationErrors, "Email should be valid\r\nPassword cannot be empty\r\n");
    }
}
