import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MainHandler extends AbstractHandler {
    private final LocalSudokuServer serverInstance;

    MainHandler(LocalSudokuServer serverInstance) {
        this.serverInstance = serverInstance;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.printf("Handling HTTP request on target: [%s]\n", target);
        if (target.equals("/terminate")) {
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
    }
}
