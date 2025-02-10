package test;

import org.testng.TestNG;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestRunner {
    public static void main(String[] args) {
        try {
            TestNG testng = new TestNG();
            String xmlPath = "testng.xml";
            
            // Сначала пробуем найти файл в текущей директории
            Path testNgFile = Paths.get(xmlPath);
            if (!Files.exists(testNgFile)) {
                // Если файл не найден в текущей директории, ищем в ресурсах
                testNgFile = Paths.get("src/test/resources/testng.xml");
            }
            
            if (!Files.exists(testNgFile)) {
                System.err.println("Ошибка: Не удалось найти файл testng.xml");
                System.exit(1);
            }
            
            testng.setTestSuites(java.util.Arrays.asList(testNgFile.toString()));
            testng.run();
            
            // Устанавливаем код возврата в зависимости от результатов тестов
            System.exit(testng.getStatus());
        } catch (Exception e) {
            System.err.println("Ошибка при запуске тестов: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
