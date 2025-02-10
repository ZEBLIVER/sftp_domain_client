package cli;

import java.io.InputStream;
import java.util.Scanner;

public class InputHandler {
    private final Scanner scanner;
    private static final String IPV4_PATTERN =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public InputHandler() {
        this.scanner = new Scanner(System.in);
    }
    public InputHandler(InputStream inputStream) {
        this.scanner = new Scanner(inputStream);
    }

    public String getNonEmptyInput(String message) {
        while (true) {
            System.out.println(message);
            String input = scanner.nextLine();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Ввод не может быть пустым.");
        }
    }

    public int getValidPort() {
        while (true) {
            System.out.println("Введите порт SFTP-сервера");
            String port = scanner.nextLine();
            try {
                int portNumber = Integer.parseInt(port);
                if (portNumber > 0 && portNumber < 65535) {
                    return portNumber;
                }
                System.out.println("Некорректный порт. Введите значение от 1 до 65535.");
            } catch (NumberFormatException e) {
                System.out.println("Порт должен быть числом.");
            }
        }
    }


    public static boolean isValidIPv4(String ip) {
        return ip != null && ip.matches(IPV4_PATTERN);
    }

    public String getMenuChoice() {
        return scanner.nextLine();
    }
}
