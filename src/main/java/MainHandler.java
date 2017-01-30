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
    private static final String CK_FIELD = "field";
    private static final String CK_HINT = "hint";

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
                final SudokuField playingField = getCookieByName(request.getCookies(), CK_FIELD)
                        .map(Cookie::getValue)
                        .map(SudokuField::new).orElse(SudokuField.getDefaultField());

                final String cell = request.getParameter("cell");
                final String value = request.getParameter("value");

                if (cell != null && value != null) try {
                    playingField.setCellValue(cell, value);
                } catch (NumberFormatException ignored) {
                }

                setHintMode(request, response, playingField);

                generateResponse(playingField, request, response);
                break;
            case "/reset":
                deleteCookie(request, response, CK_FIELD);
                deleteCookie(request, response, CK_HINT);
                response.sendRedirect("/");
                break;

            case "/terminate":
                killServer(response);
                break;
            default:
                return;
        }
        baseRequest.setHandled(true);
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        getCookieByName(request.getCookies(), name).ifPresent(c -> {
                    c.setMaxAge(0);
                    response.addCookie(c);
                }
        );
    }

    private void setHintMode(HttpServletRequest request, HttpServletResponse response, SudokuField playingField) {
        boolean showHints = getCookieByName(request.getCookies(), CK_HINT)
                .map(Cookie::getValue)
                .map("on"::equals)
                .orElse(false);

        if (request.getMethod().equals("POST")) {
            if ("on".equals(request.getParameter("hint"))) {
                System.out.println("Hints on");
                playingField.setHintMode(true);
                playingField.generateHints();
                response.addCookie(new Cookie(CK_HINT, "on"));
            } else {
                System.out.println("Hints off");
                playingField.setHintMode(false);
                response.addCookie(new Cookie(CK_HINT, "off"));
            }
        } else
            playingField.setHintMode(showHints);
    }

    private void generateResponse(SudokuField playingField, HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder responseTable = new StringBuilder();
        playingField.appendHtml(responseTable);

        final String responseHtml = tMatcher.replaceAll(responseTable.toString());

        response.addCookie(new Cookie(CK_FIELD, playingField.serialize()));

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
