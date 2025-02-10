package json;

import cli.InputHandler;
import model.DomainIpPair;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FileService {
    private InputHandler inputHandler;
    private List<DomainIpPair> data;
    private Path filePath;

    public FileService(Path filePath) {
        inputHandler = new InputHandler();
        this.filePath = filePath;
        loadDataFromFile();
    }

    private void loadDataFromFile() {
        try {
            String jsonData = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            this.data = parseJsonData(jsonData);
            System.out.println("Данные успешно скачены во временный файл.");
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
            this.data = new ArrayList<>();
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка формата файла: " + e.getMessage());
            this.data = new ArrayList<>();
        }
    }

    private List<DomainIpPair> parseJsonData(String jsonData) {
        List<DomainIpPair> pairs = new ArrayList<>();
        jsonData = jsonData.replaceAll("\\s+", ""); // Убираем все пробелы и переносы

        if (!jsonData.contains("\"addresses\":[")) {
            throw new IllegalArgumentException("Неверный формат JSON: отсутствует массив addresses");
        }

        // Извлекаем содержимое массива addresses
        String addressesContent = jsonData.split("\"addresses\":\\[")[1].split("\\]")[0];

        // Разбиваем на отдельные объекты
        String[] entries = addressesContent.split("\\},\\{");

        for (String entry : entries) {
            entry = entry.replace("{", "").replace("}", "");
            String domain = null;
            String ip = null;

            String[] fields = entry.split(",");
            for (String field : fields) {
                if (field.startsWith("\"domain\":")) {
                    domain = field.split(":")[1].replace("\"", "");
                } else if (field.startsWith("\"ip\":")) {
                    ip = field.split(":")[1].replace("\"", "");
                }
            }

            if (domain != null && ip != null) {
                pairs.add(new DomainIpPair(domain, ip));
            } else {
                throw new IllegalArgumentException("Некорректная запись в JSON: " + entry);
            }
        }
        return pairs;
    }

    public List<DomainIpPair> getDomainIpPairs() {
        return data.stream()
                .sorted(Comparator.comparing(DomainIpPair::getDomain))
                .collect(Collectors.toList());
    }


    public String getIpByDomain(String domain) {
        for (DomainIpPair pair : data) {
            if (pair.getDomain().equals(domain)) {
                return pair.getIp();
            }
        }
        return null;
    }

    public String getDomainById(String ip) {
        for (DomainIpPair pair : data) {
            if (pair.getIp().equals(ip)) {
                return pair.getDomain();
            }
        }
        return null;
    }


    public boolean addDomainIpPair(String domain, String ip) {
        if (!inputHandler.isValidIPv4(ip)) {
            throw new IllegalArgumentException("Некорректный формат IPv4: " + ip);
        }

        // Проверяем существование домена и IP в одном проходе
        for (DomainIpPair p : data) {
            if (p.getDomain().equalsIgnoreCase(domain)) {
                System.out.println("Ошибка: Домен уже существует: " + domain);
                return false;
            }
            if (p.getIp().equals(ip)) {
                System.out.println("Ошибка: IP-адрес уже существует: " + ip);
                return false;
            }
        }

        // Если дошли до этой точки, значит дубликатов нет
        data.add(new DomainIpPair(domain, ip));
        saveToFile();
        return true;
    }
    private void saveToFile() {
        StringBuilder json = new StringBuilder("{\n  \"addresses\": [\n");

        for (int i = 0; i < data.size(); i++) {
            DomainIpPair pair = data.get(i);
            json.append(String.format("    {\"domain\": \"%s\", \"ip\": \"%s\"}",
                    pair.getDomain(), pair.getIp()));
            if (i < data.size() - 1) json.append(",\n");
        }

        json.append("\n  ]\n}");

        try {
            Files.write(filePath, json.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения файла: " + e.getMessage(), e);
        }
    }


    public boolean removeDomainIpPair(String domainOrIp) {
        Iterator<DomainIpPair> iterator = data.iterator();
        while (iterator.hasNext()) {
            DomainIpPair pair = iterator.next();
            if (pair.getDomain().equals(domainOrIp) || pair.getIp().equals(domainOrIp)) {
                iterator.remove();
                saveToFile();
                return true;
            }
        }
        return false;
    }
}
