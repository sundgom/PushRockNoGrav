package pushrocks.fxui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import pushrocks.model.PushRocks;

public interface ISaveHandler {

    public Path getResourceFoldersPath(String folder);

    public PushRocks loadGame(InputStream inputStream);

    public PushRocks loadGame(String fileName, boolean isSave) throws FileNotFoundException, IOException;
    
    public PushRocks loadGame(Path filePath) throws FileNotFoundException, IOException, NumberFormatException, IllegalArgumentException;

    public void saveGame(PushRocks pushRocks, OutputStream outputStream);
    
    public void saveGame(PushRocks pushRocks, Path savePath) throws IOException;





}
