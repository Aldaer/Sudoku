import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainHandler extends AbstractHandler {
    private final LocalSudokuServer serverInstance;

    private final Matcher tMatcher = getMainTemplateMatcher();

    private static Matcher getMainTemplateMatcher() {
        try {
            final String template = IOUtils.toString(LocalSudokuServer.class.getClassLoader().getResourceAsStream("/main.html"), StandardCharsets.UTF_8);
            return Pattern.compile("<tbody>.*</tbody>", Pattern.DOTALL).matcher(template);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load template", e);
        }
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.printf("Handling HTTP request on target: [%s]\n", target);
        switch (target) {
            case "/":
                generateResponse(request, response);
                return;
            case "/terminate":
                killServer(response);
        }
    }

    private void generateResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StringBuilder responseTable = new StringBuilder();
        serverInstance.getPlayingField().appendHtml(responseTable);

        final String responseHtml = tMatcher.replaceAll(responseTable.toString());
        final PrintWriter writer = response.getWriter();
        writer.print(responseHtml);
        writer.flush();
    }

    private void killServer(HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        try {
            writer.println("Stopping Jetty server");
            writer.flush();
            response.setStatus(HttpServletResponse.SC_OK);
            serverInstance.server.stop();
        } catch (Exception e) {
            writer.println(e.getMessage());
            writer.flush();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public MainHandler(LocalSudokuServer serverInstance) {
        this.serverInstance = serverInstance;
    }
}
