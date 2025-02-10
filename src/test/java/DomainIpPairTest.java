import model.DomainIpPair;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DomainIpPairTest {
    
    @Test
    public void testDomainIpPairCreation() {
        String domain = "example.com";
        String ip = "192.168.1.1";
        DomainIpPair pair = new DomainIpPair(domain, ip);
        
        Assert.assertEquals(pair.getDomain(), domain, "Домен должен соответствовать переданному значению");
        Assert.assertEquals(pair.getIp(), ip, "IP должен соответствовать переданному значению");
    }

    @Test
    public void testToString() {
            DomainIpPair pair = new DomainIpPair("test.com", "10.0.0.1");
        String expected = "Domain: test.com, IP: 10.0.0.1";
        Assert.assertEquals(pair.toString(), expected, "Метод toString должен возвращать правильно отформатированную строку");
    }
}
