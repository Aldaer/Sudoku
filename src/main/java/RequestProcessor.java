import org.eclipse.jetty.server.Server;

import java.io.IOException;

public interface RequestProcessor {
    void processNormalRequest() throws IOException;
    void loadField() throws IOException;
    void resetField() throws IOException;
    void killServer(Server server) throws IOException;
}
