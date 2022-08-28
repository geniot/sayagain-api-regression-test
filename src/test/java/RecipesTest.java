import org.junit.Test;

public class RecipesTest extends TestBase {
    @Test
    public void shouldHaveStatus200ForAllRecipesList() {
        REQUEST.get("/recipes").then().statusCode(200);
    }

}
