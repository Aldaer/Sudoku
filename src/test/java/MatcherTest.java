import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MatcherTest {
    private String mainTemplate = "begin \n <tbody>some \ntext  </tbody> \n end";
    private String expected = "begin \n <tbody>ABC</tbody> \n end";

    private final Matcher templateMatcher = Pattern.compile("<tbody>.*</tbody>", Pattern.DOTALL).matcher(mainTemplate);

    @Test
    public void testMatchReplace() throws Exception {
        assertThat(templateMatcher.replaceAll("<tbody>ABC</tbody>"), is(expected));
    }
}
