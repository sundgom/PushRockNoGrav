package pushrock.fxui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import pushrock.model.PushRock;

public interface ISaveHandler {

    public Path getResourceFoldersPath(String folder);

    public PushRock loadGame(InputStream inputStream);

    public PushRock loadGame(String fileName, boolean isSave) throws FileNotFoundException, IOException;
    
    public PushRock loadGame(Path filePath) throws FileNotFoundException, IOException, NumberFormatException, IllegalArgumentException;

    public void saveGame(PushRock pushRock, OutputStream outputStream);
    
    public void saveGame(PushRock pushRock, Path savePath) throws IOException;
    
}
