package sniproxy;

import java.io.File;
import java.util.Optional;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class Main {

    private static final Optional<String> PORT = Optional.ofNullable(System.getenv("PORT"));
    private static final Optional<String> HOSTNAME = Optional.ofNullable(System.getenv("HOSTNAME"));
    private static final Optional<String> CONFIG_FILE = Optional.ofNullable(System.getProperty("config", System.getenv("CONFIG_FILE")));
    private static Configuration configs;

    public static void main(String[] args) throws Exception {
        // Configs
        configs = new Configuration();
        PORT.ifPresent(s -> configs.setPort(Integer.valueOf(s)));
        HOSTNAME.ifPresent(s -> configs.setHostname(s));
        CONFIG_FILE.ifPresent(s -> configs.fromXML(s));

        // Tomcat config
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(configs.getPort());
        tomcat.setHostname(configs.getHostname());

        Connector connector = new Connector(configs);
        tomcat.getService().addConnector(connector);

        // Create servlet proxy
        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

        Tomcat.addServlet(ctx, "proxy", new ProxyServlet(configs));
        // Mapping any request to servlet proxy
        ctx.addServletMappingDecoded("/*", "proxy");

        // Start tomcat
        tomcat.start();
        tomcat.getServer().await();
    }
}