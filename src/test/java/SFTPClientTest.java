import cli.InputHandler;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import sftp.SftpClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class SFTPClientTest {
    private SftpClient sftpClient;
    private final String HOST = "localhost";
    private final String PORT = "22";
    private final String USERNAME = "name";
    private final String PASSWORD = "pass";

    @BeforeMethod
    public void setup() {
        // Создаем InputHandler с подмененным InputStream
        String input = "нет\n";  // Эмулируем ввод "нет"
        InputStream in = new ByteArrayInputStream(input.getBytes());
        InputHandler inputHandler = new InputHandler(in);

        // Передаем InputHandler в SftpClient
        sftpClient = new SftpClient(inputHandler);
    }

    @Test
    public void testConnectionSuccess() {
        boolean connected = sftpClient.connectToSftpServer(HOST, PORT, USERNAME, PASSWORD);
        Assert.assertTrue(connected, "Успешное подключение к SFTP-серверу");
    }


    @Test
    public void testConnectionFailure() {
        boolean connected = sftpClient.connectToSftpServer("fake", "11", "USERNAME", "PASSWORD");
        Assert.assertFalse(connected, "Ошибка подключения к SFTP-серверу");
    }

    @Test
    public void testConnectionFailureInvalidPort() {
        boolean connected = sftpClient.connectToSftpServer(HOST, "99999", USERNAME, PASSWORD);
        Assert.assertFalse(connected, "Ошибка подключения из-за неверного порта");
    }

    @Test
    public void testConnectionFailureInvalidUsername() {
        boolean connected = sftpClient.connectToSftpServer(HOST, PORT, "fake", PASSWORD);
        Assert.assertFalse(connected, "Ошибка подключения из-за неверного логина");
    }
    @Test
    public void testConnectionFailureInvalidPassword() {
        boolean connected = sftpClient.connectToSftpServer(HOST, PORT, USERNAME, "fake");
        Assert.assertFalse(connected, "Ошибка подключения из-за неверного пароля");
    }

    @Test
    public void testOperationsWithoutConnection() {
        // Пытаемся выполнить операции без подключения
        String downloadedPath = sftpClient.downloadDomainsFile();
        Assert.assertNull(downloadedPath, "Скачивание файла без подключения должно вернуть null");

        boolean uploaded = sftpClient.uploadFileToServer();
        Assert.assertFalse(uploaded, "Загрузка файла без подключения должна вернуть false");

        // Подключаемся с неверными данными (соединение не установится)
        sftpClient.connectToSftpServer(HOST, PORT, USERNAME, "fake");
        downloadedPath = sftpClient.downloadDomainsFile();
        Assert.assertNull(downloadedPath, "Скачивание файла с неверным подключением должно вернуть null");
    }

    @Test
    public void testUploadFileToServer() {
        sftpClient.connectToSftpServer(HOST, PORT, USERNAME, PASSWORD);
        String downloadedPath = sftpClient.downloadDomainsFile(); // Сначала скачиваем, так как это требуется по логике
        Assert.assertNotNull(downloadedPath, "Ошибка при скачивании файла");
        boolean uploaded = sftpClient.uploadFileToServer();
        Assert.assertTrue(uploaded, "Ошибка при загрузке файла на сервер");
    }

    @Test
    public void testDownloadDomainsFile() {
        sftpClient.connectToSftpServer(HOST, PORT, USERNAME, PASSWORD);
        String downloadedPath = sftpClient.downloadDomainsFile();
        Assert.assertNotNull(downloadedPath, "Ошибка при скачивании файла");
        Assert.assertTrue(downloadedPath.endsWith(".json"), "Скачанный файл должен иметь расширение .json");
    }

    @Test
    public void testIsConnected() {
        sftpClient.connectToSftpServer(HOST, PORT, USERNAME, PASSWORD);
        Assert.assertTrue(sftpClient.isConnected(), "Соединение должно быть активно после подключения");
    }

    @Test
    public void testDisconnect() {
        sftpClient.connectToSftpServer(HOST, PORT, USERNAME, PASSWORD);
        sftpClient.disconnect();
        Assert.assertFalse(sftpClient.isConnected(), "После отключения соединение не должно быть активно");
    }

    @Test
    public void testUploadWithoutDownload() {
        sftpClient.connectToSftpServer(HOST, PORT, USERNAME, PASSWORD);
        boolean uploaded = sftpClient.uploadFileToServer();
        Assert.assertFalse(uploaded, "Загрузка без предварительного скачивания должна вернуть false");
    }
}
