package model;

public class DomainIpPair {
    private String domain;
    private String ip;

    public String getIp() {
        return ip;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public String toString() {
        return "Domain: " + domain + ", IP: " + ip;
    }

    public DomainIpPair(String domain, String ip) {
        this.domain = domain;
        this.ip = ip;
    }
}
