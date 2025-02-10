import cli.InputHandler;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class InputHandlerTest {
    
    @Test
    public void testValidIpv4() {
        Assert.assertTrue(InputHandler.isValidIPv4("0.0.0.0"), "Валидные пограничные значения");
        Assert.assertTrue(InputHandler.isValidIPv4("255.255.255.255"), "Валидные пограничные значения");
        Assert.assertTrue(InputHandler.isValidIPv4("192.168.1.1"), "Валидный IPv4 адрес должен быть принят");
        Assert.assertTrue(InputHandler.isValidIPv4("10.0.0.0"), "Валидный IPv4 адрес должен быть принят");
        Assert.assertTrue(InputHandler.isValidIPv4("172.16.254.1"), "Валидный IPv4 адрес должен быть принят");
    }

    @Test
    public void testInvalidIpv4() {
        Assert.assertFalse(InputHandler.isValidIPv4("256.1.2.3"), "IPv4 с числом больше 255 должен быть отклонен");
        Assert.assertFalse(InputHandler.isValidIPv4("1.1.1"), "Неполный IPv4 адрес должен быть отклонен");
        Assert.assertFalse(InputHandler.isValidIPv4("192.168.1.1.1"), "IPv4 с лишним числом должен быть отклонен");
        Assert.assertFalse(InputHandler.isValidIPv4("192.168.1"), "Неполный IPv4 адрес должен быть отклонен");
        Assert.assertFalse(InputHandler.isValidIPv4(""), "Пустая строка должна быть отклонена");
        Assert.assertFalse(InputHandler.isValidIPv4(null), "null должен быть отклонен");
        Assert.assertFalse(InputHandler.isValidIPv4("abc.def.ghi.jkl"), "Нечисловой IPv4 адрес должен быть отклонен");
    }

    @Test
    public void testGetNonEmptyInput() {
        String input = "test input\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        InputHandler handler = new InputHandler(in);
        
        String result = handler.getNonEmptyInput("Enter test:");
        Assert.assertEquals(result, "test input", "Должен вернуть введенную строку без переноса строки");
    }

    @Test
    public void testGetNonEmptyInputWithEmptyFirst() {
        String input = "\nvalid input\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        InputHandler handler = new InputHandler(in);
        
        String result = handler.getNonEmptyInput("Enter test:");
        Assert.assertEquals(result, "valid input", "Должен пропустить пустую строку и вернуть валидный ввод");
    }
}
