import model.SudokuElement;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class LocalSudokuServer extends Thread {
    static final int PORT = 80; //8888;

    final Server server = new Server(PORT);

    SudokuElement getPlayingField() {
        return null;
    }

    @Override
    public void run() {
        try {
            String webDir = this.getClass().getClassLoader().getResource("WEBINF").toExternalForm();

            ResourceHandler res = new ResourceHandler();
            res.setResourceBase(webDir);
            res.setDirAllowed(false);

            HandlerList hList = new HandlerList();
            hList.setHandlers(new Handler[]{
                    res,
                    new MainHandler(this)
                    });
            server.setHandler(hList);
            server.start();
            // server.join();
        } catch (Exception e) {
            System.out.println("Error running server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new LocalSudokuServer().start();
    }
}
