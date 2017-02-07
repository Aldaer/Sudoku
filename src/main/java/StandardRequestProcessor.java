import lombok.RequiredArgsConstructor;
import model.FieldLoader;
import model.HintMode;
import model.InvalidFieldDataException;
import model.SudokuField;
import org.eclipse.jetty.server.Server;
import template.TemplateProcessor;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
class StandardRequestProcessor implements RequestProcessor {
    private static final String CK_FIELD = "field";
    private static final String CK_HINT = "hint";

    final HttpServletRequest request;
    final HttpServletResponse response;

    @Override
    public void processNormalRequest() throws IOException {
        SudokuField playingField = getFieldFromCookies();
        final HintMode cookieHintMode = getHintModeFromCookies();

        final String cell = request.getParameter("cell");
        final String value = request.getParameter("value");
        final int colorCode = Optional.ofNullable(request.getParameter("clr"))
                .map(Integer::parseInt)
                .orElse(1);

        if (cell != null && value != null) try {
            playingField.setCellValue(cell, value, colorCode);
        } catch (NumberFormatException ignored) {
        }

        generateResponse(playingField, cookieHintMode, colorCode);
    }

    @Override
    public void processHintRequest() throws IOException {
        final HintMode cookieHintMode = getHintModeFromCookies();

        HintMode requestHintMode = Optional.ofNullable(request.getParameter("hint")).map(HintMode::of).orElse(cookieHintMode);

        System.out.printf("Hints: %s -> %s%n", cookieHintMode, requestHintMode);
        if (cookieHintMode == HintMode.OFF && requestHintMode != HintMode.OFF) {
            SudokuField playingField = getFieldFromCookies();
            playingField.activateHints();
            addFieldAsCookie(playingField);
        }

        response.addCookie(new Cookie(CK_HINT, requestHintMode.name()));
        response.sendRedirect("/");
    }

    private HintMode getHintModeFromCookies() {
        return getCookieByName(CK_HINT)
                .map(Cookie::getValue)
                .map(HintMode::of)
                .orElse(HintMode.OFF);
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

    private void generateResponse(SudokuField playingField, HintMode hintMode, int color) throws IOException {
        playingField.generateHints(hintMode);
        addFieldAsCookie(playingField);

        TemplateProcessor tp = TemplateProcessor.with(playingField, hintMode, color);
        String responseHtml = tp.process();

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
}
