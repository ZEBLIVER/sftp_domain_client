package sftp;

import cli.InputHandler;
import com.jcraft.jsch.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Vector;


public class SftpClient {
    private Session session;
    private ChannelSftp sftpChannel;
    private Path localPath;
    private InputHandler inputHandler;

    public SftpClient() {
        this.inputHandler = new InputHandler();
    }

    public SftpClient(InputHandler inputHandler) {
        this.inputHandler = inputHandler; // Позволяет передать кастомный InputHandler
    }

    // Имя файла по умолчанию, если на сервере файл не найден
    private static final String DEFAULT_FILENAME = "domains.json";

    /**
     * Проверяет активно ли SFTP соединение
     */
    private void checkConnection() {
        if (!isConnected()) {
            throw new IllegalStateException("SFTP соединение не установлено");
        }
    }

    public boolean isConnected() {
        return session != null && session.isConnected() &&
                sftpChannel != null && sftpChannel.isConnected();
    }

    /**
     * Ищет JSON файл на сервере
     * @return имя найденного файла или null если файл не найден
     */
    private String findJsonFile() throws Exception {
        checkConnection();
        try {
            Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(".");
            for (ChannelSftp.LsEntry p : list) {
                if (!p.getAttrs().isDir() && p.getFilename().endsWith(".json")){
                    return p.getFilename();
                }
            }
            System.out.println("Файл с доменами не найден на сервере. Будет создан новый файл при первой записи.");
        } catch (SftpException e) {
            System.out.println("Ошибка при поиске файла: " + e.getMessage());
        }
        return null;
    }

    /**
     * Скачивает файл с доменами с сервера
     * @return путь к локальному файлу или null в случае ошибки
     */
    public String downloadDomainsFile() {
        try {
            checkConnection();
            String remotePath = findJsonFile();
            if (remotePath == null) {
                System.out.println("На сервере нет файла с доменами");
                return null;
            }

            localPath = Files.createTempFile("domains",".json");
            localPath.toFile().deleteOnExit();

            sftpChannel.get(remotePath, localPath.toString());
            System.out.println("Файл успешно загружен с сервера.");
            return localPath.toString();
        } catch (Exception e) {
            System.out.println("Ошибка при скачивании файла: " + e.getMessage());
            return null;
        }
    }

    /**
     * Загружает обновленный файл на сервер
     * @return true если загрузка успешна, false в случае ошибки
     */
    public boolean uploadFileToServer() {
        try {
            checkConnection();

            if (localPath == null || !Files.exists(localPath)) {
                System.out.println("Нет файла для загрузки. Сначала скачайте файл.");
                return false;
            }

            String remotePath = findJsonFile();
            // Если файла нет на сервере, используем имя по умолчанию
            if (remotePath == null) {
                remotePath = DEFAULT_FILENAME;
            }

            sftpChannel.put(localPath.toString(), remotePath);
            System.out.println("Файл успешно загружен на сервер");
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке файла: " + e.getMessage());
            return false;
        }
    }

    /**
     * Подключается к SFTP серверу
     * @return true если подключение успешно, false в случае ошибки
     */
    public boolean connectToSftpServer(String address, String port, String login, String password) {
        try {
            int portNumber = Integer.parseInt(port);
            JSch jsch = new JSch();
            
            // Инициализация файла known_hosts с учетом кроссплатформенности
            try {
                Path sshDir = Paths.get(System.getProperty("user.home"), ".ssh");
                Path knownHostsPath = sshDir.resolve("known_hosts");
                
                // Создаем директорию .ssh если её нет
                if (!Files.exists(sshDir)) {
                    Files.createDirectories(sshDir);
                    // Устанавливаем права доступа только для владельца на Unix-системах
                    if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                        sshDir.toFile().setExecutable(true, true);
                        sshDir.toFile().setWritable(true, true);
                        sshDir.toFile().setReadable(true, true);
                    }
                }
                
                // Создаем файл known_hosts если его нет
                if (!Files.exists(knownHostsPath)) {
                    Files.createFile(knownHostsPath);
                    // Устанавливаем права доступа только для владельца на Unix-системах
                    if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                        knownHostsPath.toFile().setWritable(true, true);
                        knownHostsPath.toFile().setReadable(true, true);
                    }
                }
                
                jsch.setKnownHosts(knownHostsPath.toString());
                System.out.println("Файл known_hosts инициализирован: " + knownHostsPath);
            } catch (Exception e) {
                System.out.println("Ошибка при инициализации файла known_hosts: " + e.getMessage());
                return false;
            }

            session = jsch.getSession(login, address, portNumber);
            session.setPassword(password);

            String choice = inputHandler.getNonEmptyInput("Включить проверку ключей хоста? (да/нет):")
                    .trim().toLowerCase();

            Properties config = new Properties();
            if ("нет".equals(choice)) {
                config.put("StrictHostKeyChecking", "no");
            } else {
                config.put("StrictHostKeyChecking", "ask");
            }
            session.setConfig(config);
            
            // Добавляем UserInfo для обработки запросов на подтверждение ключа хоста
            session.setUserInfo(new UserInfo() {
                @Override
                public String getPassphrase() {
                    return null;
                }

                @Override
                public String getPassword() {
                    return null;
                }

                @Override
                public boolean promptPassword(String message) {
                    return false;
                }

                @Override
                public boolean promptPassphrase(String message) {
                    return false;
                }

                @Override
                public boolean promptYesNo(String message) {
                    System.out.println(message);
                    String response = inputHandler.getNonEmptyInput("Введите 'да' для подтверждения или 'нет' для отказа: ")
                            .trim().toLowerCase();
                    return "да".equals(response);
                }

                @Override
                public void showMessage(String message) {
                    System.out.println(message);
                }
            });

            session.connect();
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            System.out.println("Успешное подключение к SFTP серверу");
            return true;
        } catch (JSchException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
            if (e.getMessage().contains("UnknownHostKey")) {
                System.out.println("Подсказка: Проверьте файл known_hosts или отключите проверку ключей хоста");
            }
            return false;
        } catch (Exception e) {
            System.out.println("Неизвестная ошибка: " + e.getMessage());
            return false;
        }
    }

    /**
     * Отключается от сервера и очищает временные файлы
     */
    public void disconnect() {
        try {
            if (localPath != null && Files.exists(localPath)) {
                Files.delete(localPath);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при удалении временного файла: " + e.getMessage());
        }

        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        System.out.println("Отключение от SFTP сервера выполнено");
    }
}