package pushrocks.model;

import java.io.FileNotFoundException;

public interface SaveHandlerInterface {
    public void save(String fileName, PushRocks pushRocks) throws FileNotFoundException;
    public void load(String fileName) throws FileNotFoundException;
}
