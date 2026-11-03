package me.feseis.pbox.server;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);

    private static final List<String> WIFI_KEYWORDS =
            List.of("wi-fi", "wifi", "wlan", "wireless", "无线");

    private static final List<String> VIRTUAL_KEYWORDS =
            List.of("virtual", "direct", "vethernet", "hyper-v", "vmware", "virtualbox",
                    "docker", "wsl", "tap", "tun", "teredo", "bluetooth", "vpn",
                    "tailscale", "zerotier", "npcap");

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ServerApplication.class, args);
        printLanAddresses(context.getEnvironment());
    }

    private static void printLanAddresses(Environment env) {
        String port = env.getProperty("local.server.port",
                env.getProperty("server.port", "8080"));

        int printed = 0;
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics != null && nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                if (!isWifi(nic) || nic.getInterfaceAddresses() == null) {
                    continue;
                }
                for (InterfaceAddress address : nic.getInterfaceAddresses()) {
                    InetAddress ip = address.getAddress();
                    if (ip instanceof Inet4Address && !ip.isLoopbackAddress() && !ip.isLinkLocalAddress()) {
                        log.info("pbox server listening on hostname: http://{}:{}  [{}]",
                                ip.getHostAddress(), port, displayName(nic));
                        printed++;
                    }
                }
            }
        } catch (SocketException e) {
            log.warn("枚举网卡失败: {}", e.getMessage());
        }

        if (printed == 0) {
            log.warn("未找到 Wi-Fi 网卡，请确认电脑已连接 Wi-Fi");
        }
    }

    private static boolean isWifi(NetworkInterface nic) throws SocketException {
        if (!nic.isUp() || nic.isLoopback() || nic.isVirtual()) {
            return false;
        }
        String name = displayName(nic).toLowerCase(Locale.ROOT);
        if (VIRTUAL_KEYWORDS.stream().anyMatch(name::contains)) {
            return false;
        }
        return WIFI_KEYWORDS.stream().anyMatch(name::contains);
    }

    private static String displayName(NetworkInterface nic) {
        String name = nic.getDisplayName();
        return name == null || name.isBlank() ? nic.getName() : name;
    }
}
