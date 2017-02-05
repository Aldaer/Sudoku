package template;

import lombok.RequiredArgsConstructor;
import model.HintMode;
import model.SudokuField;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class TemplateProcessor {
    //TODO: make this field static to make template load once at startup instead of each request
    private static final String template = getMainTemplate();

    private final SudokuField playingField;
    private final HintMode hintMode;

    public static TemplateProcessor with(SudokuField playingField, HintMode hintMode) {
        return new TemplateProcessor(playingField, hintMode);
    }

    private static String getMainTemplate() {
        try {
            final ClassLoader classLoader = TemplateProcessor.class.getClassLoader();
            final InputStream resource = classLoader.getResourceAsStream("WEB-INF/main.html");
            return IOUtils.toString(resource, StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Cannot load template", e);
        }
    }

    private static Matcher templateMatcher() {
        return Pattern.compile("<tbody>.*</tbody>", Pattern.DOTALL).matcher(template);
    }

    private static Pattern radioPattern(String group, String value) {
        String regex = "(type=\"radio\"\\s+name=\"" + group + "\"\\s+value=\"" + value + "\")";
        return Pattern.compile(regex);
    }

    public String process() {
        StringBuilder responseTable = new StringBuilder();
        playingField.appendHtml(responseTable);

        String page = templateMatcher().replaceFirst(responseTable.toString());

        return checkRadioButton(page, "hint", hintMode.name());
    }

    private String checkRadioButton(String html, String group, String value) {
        return radioPattern(group, value).matcher(html).replaceAll("$1 checked");
    }
}
