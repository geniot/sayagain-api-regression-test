import io.github.geniot.sayagain.gen.model.ApiErrorDto;
import io.github.geniot.sayagain.gen.model.RecipeDto;
import io.github.geniot.sayagain.gen.model.SearchCriteriaDto;
import io.github.geniot.sayagain.gen.model.UserDto;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        ApiErrorDto expectedApiErrorDto = new ApiErrorDto();
        expectedApiErrorDto.setStatus("422 UNPROCESSABLE_ENTITY");
        expectedApiErrorDto.setMessage("Some fields are invalid.");
        List<String> expectedErrors = new ArrayList<>();
        expectedErrors.add("Email is invalid.");
        expectedErrors.add("Password cannot be empty.");
        expectedApiErrorDto.setErrors(expectedErrors);

        ApiErrorDto outApiErrorDto = signUpResponse.jsonPath().getObject("", ApiErrorDto.class);

        assertThat(expectedApiErrorDto).usingRecursiveComparison().isEqualTo(outApiErrorDto);
    }
}
