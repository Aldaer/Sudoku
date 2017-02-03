import lombok.RequiredArgsConstructor;
import model.FieldLoader;
import model.InvalidFieldDataException;
import model.SudokuField;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.MultipartConfigElement;
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
class MainHandler extends AbstractHandler {
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private final MultipartConfigElement MPCE = new MultipartConfigElement(TMP_DIR);

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
        Optional.ofNullable(request
                .getContentType())
                .filter(t -> t.startsWith("multipart"))
                .ifPresent(t -> baseRequest.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MPCE));

        System.out.printf("Processing: [%s]%n", target);
        switch (target) {
            case "/":
                processNormalRequest(request, response);
                break;

            case "/reset":
                resetField(request, response);
                break;

            case "/load":
                loadField(request, response);
                break;

            case "/terminate":
                killServer(response);
                break;
            default:
                if (mustPassToFileServer(target)) return;
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        baseRequest.setHandled(true);
    }

    void loadField(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        addFieldAsCookie(newField, response);
        deleteCookie(request, response, CK_HINT);
        response.sendRedirect("/");
    }

    private boolean mustPassToFileServer(String target) {
        Stream<String> knownFileExt = Stream.of(".htm", ".html", ".css", ".js", ".gif", ".png", ".jpg");
        final String s = target.toLowerCase();
        return knownFileExt.anyMatch(s::endsWith);
    }

    private void resetField(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final SudokuField playingField = getFieldFromCookies(request);
        playingField.reset();
        addFieldAsCookie(playingField, response);
        deleteCookie(request, response, CK_HINT);
        response.sendRedirect("/");
    }

    private void processNormalRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final SudokuField playingField = getFieldFromCookies(request);

        final String cell = request.getParameter("cell");
        final String value = request.getParameter("value");

        if (cell != null && value != null) try {
            playingField.setCellValue(cell, value);
        } catch (NumberFormatException ignored) {
        }

        setHintMode(request, response, playingField);

        generateResponse(playingField, request, response);
    }

    private SudokuField getFieldFromCookies(HttpServletRequest request) {
        return getCookieByName(request.getCookies(), CK_FIELD)
                .map(Cookie::getValue)
                .map(FieldLoader::deserializeField)
                .orElse(FieldLoader.getDefaultField());
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

        addFieldAsCookie(playingField, response);

        try (PrintWriter writer = response.getWriter()) {
            writer.print(responseHtml);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private void addFieldAsCookie(SudokuField playingField, HttpServletResponse response) {
        response.addCookie(new Cookie(CK_FIELD, playingField.serialize()));
    }

    private static Optional<Cookie> getCookieByName(Cookie[] cookies, String name) {
        if (cookies == null) return Optional.empty();
        return Stream.of(cookies)
                .filter(c -> c.getName().equals(name))
                .findAny();
    }

    private void killServer(HttpServletResponse response) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Stopping Jetty server");
            writer.close();
            serverInstance.server.stop();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
