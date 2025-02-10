import json.FileService;
import model.DomainIpPair;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileServiceTest {
    private FileService fileService;
    private Path tempFile;

    @BeforeMethod
    public void setup() throws Exception {
        // Создаем временный файл с тестовыми данными
        tempFile = Files.createTempFile("test_domains", ".json");
        String initialJson = "{\n" +
                "  \"addresses\": [\n" +
                "    {\"domain\": \"example.com\", \"ip\": \"192.168.1.1\"},\n" +
                "    {\"domain\": \"test.com\", \"ip\": \"10.0.0.1\"}\n" +
                "  ]\n}";
        Files.write(tempFile, initialJson.getBytes());
        fileService = new FileService(tempFile);
    }

    @Test
    public void testGetDomainIpPairs() {
        List<DomainIpPair> pairs = fileService.getDomainIpPairs();
        Assert.assertEquals(pairs.size(), 2, "Должно быть загружено 2 пары");
        // Проверяем сортировку по домену
        Assert.assertEquals(pairs.get(0).getDomain(), "example.com");
        Assert.assertEquals(pairs.get(1).getDomain(), "test.com");
    }

    @Test
    public void testGetIpByDomain() {
        String ip = fileService.getIpByDomain("example.com");
        Assert.assertEquals(ip, "192.168.1.1", "IP адрес должен соответствовать домену");
        
        String nonExistentIp = fileService.getIpByDomain("nonexistent.com");
        Assert.assertNull(nonExistentIp, "Для несуществующего домена должен возвращаться null");
    }

    @Test
    public void testGetDomainById() {
        String domain = fileService.getDomainById("10.0.0.1");
        Assert.assertEquals(domain, "test.com", "Домен должен соответствовать IP адресу");
        
        String nonExistentDomain = fileService.getDomainById("1.1.1.1");
        Assert.assertNull(nonExistentDomain, "Для несуществующего IP должен возвращаться null");
    }

    @Test
    public void testAddDomainIpPair() {
        boolean added = fileService.addDomainIpPair("new.com", "172.16.0.1");
        Assert.assertTrue(added, "Добавление новой уникальной пары должно быть успешным");
        
        String ip = fileService.getIpByDomain("new.com");
        Assert.assertEquals(ip, "172.16.0.1", "Новая пара должна быть найдена в данных");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddInvalidIp() {
        fileService.addDomainIpPair("invalid.com", "256.256.256.256");
    }

    @Test
    public void testAddDuplicateDomain() {
        boolean added = fileService.addDomainIpPair("example.com", "172.16.0.1");
        Assert.assertFalse(added, "Добавление дубликата домена должно быть отклонено");
    }

    @Test
    public void testAddDuplicateIp() {
        boolean added = fileService.addDomainIpPair("unique.com", "192.168.1.1");
        Assert.assertFalse(added, "Добавление дубликата IP должно быть отклонено");
    }

    @Test
    public void testRemoveDomainIpPair() {
        boolean removed = fileService.removeDomainIpPair("example.com");
        Assert.assertTrue(removed, "Удаление существующей пары должно быть успешным");
        
        String ip = fileService.getIpByDomain("example.com");
        Assert.assertNull(ip, "После удаления пара не должна быть найдена");
    }

    @Test
    public void testRemoveByIp() {
        boolean removed = fileService.removeDomainIpPair("10.0.0.1");
        Assert.assertTrue(removed, "Удаление по IP должно быть успешным");
        
        String domain = fileService.getDomainById("10.0.0.1");
        Assert.assertNull(domain, "После удаления пара не должна быть найдена");
    }

    @Test
    public void testRemoveNonExistent() {
        boolean removed = fileService.removeDomainIpPair("nonexistent.com");
        Assert.assertFalse(removed, "Удаление несуществующей пары должно вернуть false");
    }

    @Test
    public void testEmptyFile() throws Exception {
        // Создаем пустой файл
        Path emptyFile = Files.createTempFile("empty_test", ".json");
        Files.write(emptyFile, "".getBytes());
        
        FileService service = new FileService(emptyFile);
        List<DomainIpPair> pairs = service.getDomainIpPairs();
        Assert.assertTrue(pairs.isEmpty(), "Список пар из пустого файла должен быть пустым");
    }

    @Test
    public void testInvalidJsonFormat() throws Exception {
        // Создаем файл с некорректным JSON
        Path invalidFile = Files.createTempFile("invalid_test", ".json");
        String invalidJson = "{ invalid json content }";
        Files.write(invalidFile, invalidJson.getBytes());
        
        FileService service = new FileService(invalidFile);
        List<DomainIpPair> pairs = service.getDomainIpPairs();
        Assert.assertTrue(pairs.isEmpty(), "Список пар из некорректного файла должен быть пустым");
    }

    @Test
    public void testMissingAddressesArray() throws Exception {
        // JSON без массива addresses
        Path invalidFile = Files.createTempFile("no_addresses_test", ".json");
        String invalidJson = "{ \"other\": [] }";
        Files.write(invalidFile, invalidJson.getBytes());
        
        FileService service = new FileService(invalidFile);
        List<DomainIpPair> pairs = service.getDomainIpPairs();
        Assert.assertTrue(pairs.isEmpty(), "Список пар из файла без addresses должен быть пустым");
    }
}
