import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.ResourceHandler;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class LocalSudokuServer extends Thread {
    static final int PORT =
            Optional.ofNullable(System.getenv("PORT"))
                    .map(Integer::valueOf)
                    .orElse(8080);

    final Server server = new Server(PORT);

    @Override
    public void run() {
        try {
            HandlerList hList = new HandlerList();
            hList.setHandlers(createHandlers());
            server.setHandler(hList);
            server.start();
            System.out.println("Running server on port " + PORT);
        } catch (Exception e) {
            System.out.println("Error running server");
            e.printStackTrace();
        }
    }

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private final MultipartConfigElement MPCE = new MultipartConfigElement(TMP_DIR, 1024, -1L, Integer.MAX_VALUE);

    Handler[] createHandlers() {
        ResourceHandler res = new ResourceHandler();
        @SuppressWarnings("ConstantConditions")
        String webDir = this.getClass().getClassLoader().getResource("WEB-INF").toExternalForm();
        res.setResourceBase(webDir);
        res.setDirAllowed(true);

        Handler main = new ParsingHandler(server);

        Handler multipartFixer = multipartSafeWrapper(main);

        return new Handler[]{multipartFixer, res};
    }

    Handler multipartSafeWrapper(Handler main) {
        HandlerWrapper multipartFixer = new HandlerWrapper() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                Optional.ofNullable(request
                        .getContentType())
                        .filter(t -> t.startsWith("multipart"))
                        .ifPresent(t -> baseRequest.setAttribute(Request.MULTIPART_CONFIG_ELEMENT, MPCE));
                super.handle(target, baseRequest, request, response);
            }
        };

        multipartFixer.setHandler(main);
        return multipartFixer;
    }

    public static void main(String[] args) {
        new LocalSudokuServer().start();
    }
}
