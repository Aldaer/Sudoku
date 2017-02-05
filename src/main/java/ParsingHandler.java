import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ParsingHandler extends AbstractHandler {
    private final Server serverInstance;
    private final BiFunction<HttpServletRequest, HttpServletResponse, RequestProcessor> requestProcessorFactory;

    ParsingHandler(Server serverInstance) {
        this(serverInstance, StandardRequestProcessor::new);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.printf("Processing %s: [%s]%n", request.getMethod(), target);
        RequestProcessor proc = requestProcessorFactory.apply(request, response);

        switch (target) {
            case "/":
                proc.processNormalRequest();
                break;

            case "/hint":
                proc.processHintRequest();
                break;

            case "/reset":
                proc.resetField();
                break;

            case "/load":
                proc.loadField();
                break;

            case "/terminate":
                proc.killServer(serverInstance);
                break;
            default:
                if (mustPassToFileServer(target)) return;
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        baseRequest.setHandled(true);
    }

    private static boolean mustPassToFileServer(String target) {
        Stream<String> knownFileExt = Stream.of(".htm", ".html", ".css", ".js", ".gif", ".png", ".jpg");
        final String s = target.toLowerCase();
        return knownFileExt.anyMatch(s::endsWith);
    }

}
