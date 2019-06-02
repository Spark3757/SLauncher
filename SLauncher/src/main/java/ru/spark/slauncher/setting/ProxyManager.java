package ru.spark.slauncher.setting;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableObjectValue;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.StringUtils;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;

import static ru.spark.slauncher.setting.ConfigHolder.config;

public final class ProxyManager {
    private static ObjectBinding<Proxy> proxyProperty;

    private ProxyManager() {
    }

    public static Proxy getProxy() {
        return proxyProperty.get();
    }

    public static ObservableObjectValue<Proxy> proxyProperty() {
        return proxyProperty;
    }

    static void init() {
        proxyProperty = Bindings.createObjectBinding(
                () -> {
                    String host = config().getProxyHost();
                    int port = config().getProxyPort();
                    if (!config().hasProxy() || StringUtils.isBlank(host) || config().getProxyType() == Proxy.Type.DIRECT) {
                        return Proxy.NO_PROXY;
                    } else {
                        if (port < 0 || port > 0xFFFF) {
                            Logging.LOG.warning("Illegal proxy port: " + port);
                            return Proxy.NO_PROXY;
                        }
                        return new Proxy(config().getProxyType(), new InetSocketAddress(host, port));
                    }
                },
                config().proxyTypeProperty(),
                config().proxyHostProperty(),
                config().proxyPortProperty(),
                config().hasProxyProperty());

        proxyProperty.addListener(any -> updateSystemProxy());
        updateSystemProxy();

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (config().hasProxyAuth()) {
                    String username = config().getProxyUser();
                    String password = config().getProxyPass();
                    if (username != null && password != null) {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                }
                return null;
            }
        });
    }

    private static void updateSystemProxy() {
        Proxy proxy = proxyProperty.get();
        if (proxy.type() == Proxy.Type.DIRECT) {
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
        } else {
            InetSocketAddress address = (InetSocketAddress) proxy.address();
            String host = address.getHostString();
            String port = String.valueOf(address.getPort());
            if (proxy.type() == Type.HTTP) {
                System.clearProperty("socksProxyHost");
                System.clearProperty("socksProxyPort");
                System.setProperty("http.proxyHost", host);
                System.setProperty("http.proxyPort", port);
                System.setProperty("https.proxyHost", host);
                System.setProperty("https.proxyPort", port);
            } else if (proxy.type() == Type.SOCKS) {
                System.clearProperty("http.proxyHost");
                System.clearProperty("http.proxyPort");
                System.clearProperty("https.proxyHost");
                System.clearProperty("https.proxyPort");
                System.setProperty("socksProxyHost", host);
                System.setProperty("socksProxyPort", port);
            }
        }
    }
}
