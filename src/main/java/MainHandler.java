import model.SudokuElement;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainHandler extends AbstractHandler {
    private final LocalSudokuServer serverInstance;

    private final Matcher tMatcher = getMainTemplateMatcher();

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
            case "/hint":
                generateResponse(request, response);
                break;
            case "/terminate":
                killServer(response);
                break;
            default:
                return;
        }
        baseRequest.setHandled(true);
    }

    private void generateResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final Cookie[] cookies = request.getCookies();

        StringBuilder responseTable = new StringBuilder();
        getPlayingField(cookies).appendHtml(responseTable);

        final String responseHtml = tMatcher.replaceAll(responseTable.toString());
        try (PrintWriter writer = response.getWriter()) {
            writer.print(responseHtml);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private SudokuElement getPlayingField(Cookie[] cookies) {
        String[] sx = {
                "--179-2-6",
                "73-21-98-",
                "926-543--",
                "-781-5-9-",
                "31-489--7",
                "54---7128",
                "1-7-62-5-",
                "--5971-3-",
                "2635--7--"};
        return new SudokuField(sx);
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

    public MainHandler(LocalSudokuServer serverInstance) {
        this.serverInstance = serverInstance;
    }
}
