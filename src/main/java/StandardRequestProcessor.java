import lombok.RequiredArgsConstructor;
import model.FieldLoader;
import model.HintMode;
import model.InvalidFieldDataException;
import model.SudokuField;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor
class StandardRequestProcessor implements RequestProcessor {
    private static final String CK_FIELD = "field";
    private static final String CK_HINT = "hint";

    //TODO: make this field static to make template load once at startup instead of each request
    private final String template = getMainTemplate();

    final HttpServletRequest request;
    final HttpServletResponse response;

    @Override
    public void processNormalRequest() throws IOException {
        final SudokuField playingField = getFieldFromCookies();

        final String cell = request.getParameter("cell");
        final String value = request.getParameter("value");

        if (cell != null && value != null) try {
            playingField.setCellValue(cell, value);
        } catch (NumberFormatException ignored) {
        }

        setHintMode(playingField);

        generateResponse(playingField);
    }

    private SudokuField getFieldFromCookies() {
        return getCookieByName(CK_FIELD)
                .map(Cookie::getValue)
                .map(FieldLoader::deserializeField)
                .orElse(FieldLoader.getDefaultField());
    }

    @Override
    public void loadField() throws IOException {
        SudokuField newField;
        try {
            final Part filePart = request.getPart("file");
            try (InputStream is = filePart.getInputStream()) {
                byte[] buf = new byte[1024];
                final int len = is.read(buf);
                String contents = new String(buf, 0, len);
                newField = FieldLoader.getFieldFromString(contents);
            }
        } catch (ServletException | InvalidFieldDataException e) {
            newField = FieldLoader.getEmptyField();
        }
        addFieldAsCookie(newField);
        deleteCookie(CK_HINT);
        response.sendRedirect("/");
    }

    @Override
    public void resetField() throws IOException {
        final SudokuField playingField = getFieldFromCookies();
        playingField.reset();
        addFieldAsCookie(playingField);
        deleteCookie(CK_HINT);
        response.sendRedirect("/");
    }

    private void deleteCookie(String name) {
        getCookieByName(name).ifPresent(c -> {
                    c.setMaxAge(0);
                    response.addCookie(c);
                }
        );
    }

    private void setHintMode(SudokuField playingField) {
        final HintMode cookieHintMode = getCookieByName(CK_HINT)
                .map(Cookie::getValue)
                .map(HintMode::of)
                .orElse(HintMode.OFF);

        if (!request.getMethod().equals("POST")) {
            playingField.setHintMode(cookieHintMode);
            return;
        }

        final HintMode hintMode = HintMode.of(request.getParameter("hint"));
        System.out.println("Hints=" + hintMode);
        playingField.setHintMode(hintMode);
        switch (hintMode) {
            case ON:
                break;
            case SMART:
                break;
            case OFF:
        }
        // TODO: implement server-side hint processing
}

    private void generateResponse(SudokuField playingField) throws IOException {
        StringBuilder responseTable = new StringBuilder();
        playingField.appendHtml(responseTable);

        final String responseHtml = templateMatcher().replaceAll(responseTable.toString());

        addFieldAsCookie(playingField);

        try (PrintWriter writer = response.getWriter()) {
            writer.print(responseHtml);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private void addFieldAsCookie(SudokuField playingField) {
        response.addCookie(new Cookie(CK_FIELD, playingField.serialize()));
    }

    private Optional<Cookie> getCookieByName(String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Stream.of(cookies)
                .filter(c -> c.getName().equals(name))
                .findAny();
    }

    @Override
    public void killServer(Server server) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Stopping Jetty server");
            writer.close();
            server.stop();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Matcher templateMatcher() {
        return Pattern.compile("<tbody>.*</tbody>", Pattern.DOTALL).matcher(template);
    }

    private static String getMainTemplate() {
        try {
            final ClassLoader classLoader = ParsingHandler.class.getClassLoader();
            final InputStream resource = classLoader.getResourceAsStream("WEB-INF/main.html");
            return IOUtils.toString(resource, StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Cannot load template", e);
        }
    }
}
