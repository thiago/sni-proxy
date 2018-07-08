package sniproxy;

import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

class Connector extends org.apache.catalina.connector.Connector {
    Connector(Configuration configs) throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException {
        super();

        this.setPort(configs.getPort());
        this.setAttribute("clientAuth", "false");
        this.setAttribute("protocol", "HTTP/1.1");
        this.setAttribute("sslProtocol", "TLS");
        this.setAttribute("maxThreads", "200");
        this.setAttribute("protocol", "org.apache.coyote.http11.Http11AprProtocol");

        // Setup sni if enabled
        if (configs.getSniEnabled()) {

            // Load keystore
            KeyStore store = KeyStore.getInstance(configs.getKeystoreType());
            try (InputStream is = Files.newInputStream(Paths.get(configs.getKeystoreFile()))) {
                store.load(is, configs.getKeystorePassword().toCharArray());
            }
            Enumeration<String> storeAliases = store.aliases();

            // Setup connector with ssl mode and keystore
            this.setSecure(true);
            this.setScheme("https");
            this.setAttribute("SSLEnabled", true);

            if (configs.getKeystoreDefaultAlias() != null){
                this.setAttribute("keyAlias", configs.getKeystoreDefaultAlias());
            }

            this.setAttribute("keystorePass", configs.getKeystorePassword());
            this.setAttribute("keystoreType", store.getType());
            this.setAttribute("keystoreFile", new File(configs.getKeystoreFile()).getAbsolutePath());
            // Create a host config per keystore alias
            while (storeAliases.hasMoreElements()) {
                String fqdn = storeAliases.nextElement();

                SSLHostConfig ssl = new SSLHostConfig();
                ssl.setHostName(fqdn);
                SSLHostConfigCertificate cert = new SSLHostConfigCertificate(ssl, SSLHostConfigCertificate.Type.RSA);

                cert.setCertificateKeystore(store);
                cert.setCertificateKeyAlias(fqdn);
                cert.setCertificateKeystoreType(store.getType());
                cert.setCertificateKeystorePassword(configs.getKeystorePassword());
                ssl.addCertificate(cert);
                this.addSslHostConfig(ssl);
            }
        }
    }
}