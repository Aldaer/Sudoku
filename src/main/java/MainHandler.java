import model.SudokuField;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class MainHandler extends AbstractHandler {
    private final LocalSudokuServer serverInstance;

    private final Matcher tMatcher = getMainTemplateMatcher();
    private static final String FIELD = "field";

    private static Matcher getMainTemplateMatcher() {
        try {
            final ClassLoader classLoader = MainHandler.class.getClassLoader();
            final InputStream resource = classLoader.getResourceAsStream("WEB-INF/main.html");
            final String template = IOUtils.toString(resource, StandardCharsets.UTF_8);
            return Pattern.compile("<tbody>.*</tbody>", Pattern.DOTALL).matcher(template);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Cannot load template", e);
        }
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.printf("Handling HTTP request on target: [%s]\n", target);
        switch (target) {
            case "/":
                final SudokuField playingField = getCookieByName(request.getCookies(), FIELD)
                        .map(Cookie::getValue)
                        .map(SudokuField::new).orElse(SudokuField.getDefaultField());
                final Cookie numberC = getCookieByName(request.getCookies(), "number").orElse(new Cookie("number", "0"));
                numberC.setValue(String.valueOf(Integer.parseInt(numberC.getValue()) + 1));
                System.out.println("Request: " + numberC.getValue());
                response.addCookie(numberC);

                final String cell = request.getParameter("cell");
                final String value = request.getParameter("value");

                if (cell != null && value != null) try {
                    playingField.setCellValue(cell, value);
                } catch (NumberFormatException ignored) {}

                if ("on".equals(request.getParameter("hint")))
                    playingField.generateHints();
                else
                    playingField.clearHints();

                generateResponse(playingField, request, response);
                break;
            case "/terminate":
                killServer(response);
                break;
            default:
                return;
        }
        baseRequest.setHandled(true);
    }

    private void generateResponse(SudokuField playingField, HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder responseTable = new StringBuilder();
        playingField.appendHtml(responseTable);

        final String responseHtml = tMatcher.replaceAll(responseTable.toString());

        response.addCookie(new Cookie(FIELD, playingField.serialize()));

        try (PrintWriter writer = response.getWriter()) {
            writer.print(responseHtml);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private Optional<Cookie> getCookieByName(Cookie[] cookies, String name) {
        if (cookies == null) return Optional.empty();
        return Stream.of(cookies)
                .filter(c -> c.getName().equals(name))
                .findAny();
    }

    private void killServer(HttpServletResponse response) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Stopping Jetty server");
            response.setStatus(HttpServletResponse.SC_OK);
            serverInstance.server.stop();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    MainHandler(LocalSudokuServer serverInstance) {
        this.serverInstance = serverInstance;
    }
}
