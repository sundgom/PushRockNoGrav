package pushrocks.fxui;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import pushrocks.model.PushRocks;

public interface ISaveHandler {
    public void saveGame(Path filePath, PushRocks pushrocks) throws FileNotFoundException;

    public void loadGame(Path filePath, PushRocks pushrocks) throws FileNotFoundException;

    public void loadGameSave(Path filePath, PushRocks pushrocks) throws FileNotFoundException;
    
    public void loadGameLevel(String levelName, PushRocks pushrocks) throws FileNotFoundException;

    public Path getResourceFoldersPath(String folder);
}
