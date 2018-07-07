package sniproxy;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.Iterator;
import java.util.logging.Logger;

public class Configuration {

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());
    private Integer port = 8080;
    private String hostname = "localhost";
    private Boolean sniEnabled = false;
    private String keystoreFile = null;
    private String keystoreType = "JKS";
    private String keystorePassword = null;
    private String keystoreDefaultAlias = null;

    private XMLConfiguration configs = new XMLConfiguration();

    public Configuration() {
    }

    public void fromXML(String fileName) {

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
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }

    public Boolean getSniEnabled() {
        return sniEnabled;
    }

    public String getKeystoreFile() {
        return keystoreFile;
    }

    public String getKeystoreDefaultAlias() {
        return this.keystoreDefaultAlias;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }
}
