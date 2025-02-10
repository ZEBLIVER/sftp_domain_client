package cli;

import json.FileService;
import model.DomainIpPair;
import sftp.SftpClient;
import java.nio.file.Paths;
import java.util.List;

public class CommandExecutor {
    private final SftpClient sftpClient;
    private FileService fileService;
    private final InputHandler inputHandler;

    public CommandExecutor(SftpClient sftpClient) {
        this.sftpClient = sftpClient;
        this.inputHandler = new InputHandler();
    }

    boolean connectToSftp() {
        boolean connected = false;
        boolean continueTrying = true;

        while (!connected && continueTrying) {
            try {
                String address = inputHandler.getNonEmptyInput("Введите адрес SFTP-сервера");
                int port = inputHandler.getValidPort();
                String login = inputHandler.getNonEmptyInput("Введите логин SFTP-сервера");
                String password = inputHandler.getNonEmptyInput("Введите пароль SFTP-сервера");
                
                try {
                    connected = sftpClient.connectToSftpServer(address, String.valueOf(port), login, password);
                } catch (Exception e) {
                    System.out.println("Ошибка подключения: " + e.getMessage());
                }
                
                if (!connected) {
                    String response = inputHandler.getNonEmptyInput("Желаете продолжить? да/нет");
                    if (!response.equalsIgnoreCase("да")) {
                        continueTrying = false;
                    }
                }
            } catch (Exception e) {
                System.out.println("Ошибка ввода данных: " + e.getMessage());
                String response = inputHandler.getNonEmptyInput("Желаете продолжить? да/нет");
                if (!response.equalsIgnoreCase("да")) {
                    continueTrying = false;
                }
            }
        }
        return connected;
    }

    public boolean initializeFileService() {
        if (fileService != null) return true;

        String localPath = sftpClient.downloadDomainsFile();
        if (localPath == null) {
            System.out.println("Не удалось загрузить файл с сервера.");
            return false;
        }

        try {
            fileService = new FileService(Paths.get(localPath));
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
            return false;
        }
    }

    public void showDomainIpPairs() {
        List<DomainIpPair> pairs = fileService.getDomainIpPairs();
        if (pairs.isEmpty()) {
            System.out.println("Список пар пуст");
            return;
        }
        System.out.println("Список доменов и IP-адресов:");
        pairs.forEach(System.out::println);
    }

    public void getIpByDomain() {
        String domain = inputHandler.getNonEmptyInput("Введите доменное имя");
        String ip = fileService.getIpByDomain(domain);
        if (ip != null) {
            System.out.println("IP-адрес: " + ip);
        } else System.out.println("Домен не найден.");
    }

    public void getDomainByIp() {
        String ip = inputHandler.getNonEmptyInput("Введите IP-адрес");
        String domain = fileService.getDomainById(ip);
        if (domain != null) {
            System.out.println("Домен: " + domain);
        } else System.out.println("IP-адрес не найден.");
    }

    public void addNewPair() {
        try {
            String domain = inputHandler.getNonEmptyInput("Введите доменное имя");
            String ip = inputHandler.getNonEmptyInput("Введите IP-адрес");
            if (fileService.addDomainIpPair(domain, ip)) {
                if (sftpClient.uploadFileToServer()) {
                    System.out.println("Пара успешно добавлена");
                } else {
                    System.out.println("Ошибка при сохранении на сервер");
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка:" + e.getMessage());
        }
    }

    public void removePair() {
        String value = inputHandler.getNonEmptyInput("Введите домен или IP для удаления");
        if (fileService.removeDomainIpPair(value)) {
            if (sftpClient.uploadFileToServer()) {
                System.out.println("Пара успешно удалена");
            } else {
                System.out.println("Ошибка при сохранении на сервер");
            }
        } else {
            System.out.println("Пара не найдена");
        }

    }

    public void disconect() {
        sftpClient.disconnect();
    }

    public boolean isConnected() {
        return sftpClient.isConnected();
    }
}
