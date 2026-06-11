package main.Server.FileManagment;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import main.BasicClasses.Coordinates;
import main.BasicClasses.Flat;
import main.BasicClasses.House;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileManager {
    private static final Logger logger = LogManager.getLogger(FileManager.class);
    private final String filePath;
    private boolean canWrite = false;

    public FileManager() {
        filePath = System.getenv("FILE_PATH");
    }

    public Vector<Flat> read_from_file() {
        XStream xStream = new XStream(new DomDriver());
        xStream.allowTypes(new Class[]{Flat.class, House.class, Coordinates.class});
        try {
            Scanner scanner = new Scanner(new File(System.getenv("FILE_PATH")));
            StringBuilder xml = new StringBuilder();
            while (scanner.hasNextLine()) {
                xml.append(scanner.nextLine());
            }
            scanner.close();
            Vector<Flat> collection = (Vector<Flat>) xStream.fromXML(xml.toString());
            canWrite = true;
            logger.info("Файл прочитан: {}", System.getenv("FILE_PATH"));
            return collection;
        } catch (FileNotFoundException | NullPointerException e1) {
            logger.error("Файл не найден", e1);
            System.out.println("Файл не найден!");
        }
        canWrite = false;
        return new Vector<>();
    }

    public synchronized void write_to_file(Vector<Flat> c1) {
        if (!canWrite) {
            logger.warn("Сохранение отменено, файл не был прочитан");
            System.out.println("Файл не сохранён, потому что он не был нормально прочитан!");
            return;
        }

        XStream xStream = new XStream(new DomDriver());
        xStream.allowTypes(new Class[]{Flat.class, House.class, Coordinates.class});
        try {
            File file = new File(filePath);
            if (file.exists() && file.length() > 0) {
                Files.copy(file.toPath(), new File(filePath + ".bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            String x1 = xStream.toXML(c1);
            FileWriter writer = new FileWriter(filePath, false);
            writer.write(x1);
            writer.close();
            logger.info("Файл сохранён: {}", filePath);
        } catch (FileNotFoundException | NullPointerException e1) {
            logger.error("Файл не найден", e1);
            System.out.println("Файл не найден!");
        } catch (IOException e2) {
            logger.error("Ошибка записи файла", e2);
            System.out.println("Файл не найден!");
        }
    }
}
