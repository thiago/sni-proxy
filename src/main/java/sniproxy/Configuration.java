package sniproxy;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.*;
import java.util.logging.Logger;

class Configuration {

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());
    private Integer port = 8080;
    private String hostname = "localhost";
    private Boolean sniEnabled = false;
    private String keystoreFile = null;
    private String keystoreType = "JKS";
    private String keystorePassword = null;
    private String keystoreDefaultAlias = null;
    private Map<String, Route> routes = new HashMap<String, Route>();
    private XMLConfiguration configs = new XMLConfiguration();

    Configuration() {
    }

    void fromXML(String fileName) {

        // Load configuration file
        Configurations configs = new Configurations();
        try {
            this.configs = configs.xml(fileName);
        } catch (ConfigurationException as) {
            LOGGER.warning(as.toString());
        }

        this.port = this.configs.getInt("port", this.port);
        this.hostname = this.configs.getString("hostname", this.hostname);

        // Load SNI configuration if there present
        Iterator<String> sni = this.configs.getKeys("sni");
        this.sniEnabled = sni.hasNext();
        if (this.sniEnabled) {
            this.keystoreFile = this.configs.getString("sni.file");
            this.keystorePassword = this.configs.getString("sni.password");
            this.keystoreDefaultAlias = this.configs.getString("sni.alias");
            this.keystoreType = this.configs.getString("sni.type", this.keystoreType);

            if (this.keystoreFile == null) {
                LOGGER.warning("To enable support of SNI you must specify a keystore file");
            }
        }

        // Load routes
        List<Object> fqdns = this.configs.getList("routes.route.fqdn");
        for (int i = 0; i < fqdns.size(); i++) {
            String fqdn = this.configs.getString("routes.route(" + i + ").fqdn");
            String[] targets = this.configs.getStringArray("routes.route(" + i + ").targets.target");
            Boolean preserveCookies = this.configs.getBoolean("routes.route(" + i + ").preserveCookies", true);
            Boolean preserveHost = this.configs.getBoolean("routes.route(" + i + ").preserveHost", true);
            Boolean forwardIP = this.configs.getBoolean("routes.route(" + i + ").forwardIP", true);
            Route route = new Route(fqdn, targets);
            route.setForwardIP(forwardIP);
            route.setPreserveCookies(preserveCookies);
            route.setPreserveHost(preserveHost);
            this.addRoute(route);
        }

    }

    private void addRoute(Route route) {
        this.routes.put(route.getFqdn(), route);
    }

    Route getRoute(String fqdn) {
        return this.routes.get(fqdn);
    }

    void setPort(Integer port) {
        this.port = port;
    }

    void setHostname(String hostname) {
        this.hostname = hostname;
    }

    Integer getPort() {
        return port;
    }

    String getHostname() {
        return hostname;
    }

    Boolean getSniEnabled() {
        return sniEnabled;
    }

    String getKeystoreFile() {
        return keystoreFile;
    }

    String getKeystoreDefaultAlias() {
        return this.keystoreDefaultAlias;
    }

    String getKeystoreType() {
        return keystoreType;
    }

    String getKeystorePassword() {
        return keystorePassword;
    }
}
