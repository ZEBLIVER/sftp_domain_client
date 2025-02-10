package cli;

import sftp.SftpClient;

public class ConsoleUI {
    private final SftpClient sftpClient;
    private final InputHandler inputHandler;
    private final CommandExecutor commandExecutor;

    public ConsoleUI() {
        this.sftpClient = new SftpClient();
        this.inputHandler = new InputHandler();
        this.commandExecutor = new CommandExecutor(sftpClient);
    }

    public void start() {
        boolean isRunning = true;
        while (isRunning) {
            try {
                // Проверяем соединение с SFTP
                if (!commandExecutor.isConnected()) {
                    System.out.println("Для начала работы с программой введите данные для подключения к sftp-серверу.");
                    if (!commandExecutor.connectToSftp()) {
                        break;
                    }
                }

                // Загружаем файл если нужно
                if (!commandExecutor.initializeFileService()) {
                    break;
                }

                // Показываем меню и обрабатываем выбор
                showConsoleMenu();
                String choice = inputHandler.getMenuChoice();
                switch (choice) {
                    case "1":
                        commandExecutor.showDomainIpPairs();
                        break;
                    case "2":
                        commandExecutor.getIpByDomain();
                        break;
                    case "3":
                        commandExecutor.getDomainByIp();
                        break;
                    case "4":
                        commandExecutor.addNewPair();
                        break;
                    case "5":
                        commandExecutor.removePair();
                        break;
                    case "6":
                        commandExecutor.disconect();
                        isRunning = false;
                        break;
                    default:
                        System.out.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (Exception e) {
                System.out.println("Произошла ошибка: " + e.getMessage());
                System.out.println("Попробуйте переподключиться к серверу.");
                sftpClient.disconnect();
                isRunning = false;
            }
        }

        System.out.println("Работа завершена.");
    }




    private void showConsoleMenu() {
        System.out.println("Соединение с SFTP-сервером успешно установлено. Выберите действие:");
        System.out.println("1. Получение списка пар \"домен – адрес\" из файла");
        System.out.println("2. Получение IP-адреса по доменному имени");
        System.out.println("3. Получение доменного имени по IP-адресу");
        System.out.println("4. Добавление новой пары \"домен – адрес\" в файл");
        System.out.println("5. Удаление пары \"домен – адрес\" по доменному имени или IP-адресу");
        System.out.println("6. Завершение работы");
    }




}