package pushrocks.model;

import java.io.FileNotFoundException;

public class SaveHandler implements SaveHandlerInterface {

    @Override
    public void save(String fileName, PushRocks pushRocks) throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(new File(getFilePath(fileName))) {
            writer.println(pushRocks.get)
        }
        
    }

    @Override
    public void load(String fileName) throws FileNotFoundException {
        // TODO Auto-generated method stub
        
    }
    
    private static String getFilePath(String fileName, boolean isSave) {
        if (isSave) {
            return SaveHandler.class.getResource("saves/").getFile() + fileName + ".txt";
        }
        else {
            return SaveHandler.class.getResource("levels/").getFile() + fileName + ".txt";
        }
    }
}
