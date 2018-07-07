package sniproxy;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class Main {

    private static final Optional<String> PORT = Optional.ofNullable(System.getenv("PORT"));
    private static final Optional<String> HOSTNAME = Optional.ofNullable(System.getenv("HOSTNAME"));

    public static void main(String[] args) throws Exception {

        // Configs
        Integer port = Integer.valueOf(PORT.orElse("8080"));
        String hostname = HOSTNAME.orElse("localhost");

        // Tomcat config
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setHostname(hostname);

        // Create servlet proxy
        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
        Tomcat.addServlet(ctx, "proxy", new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/html");
                try (Writer writer = response.getWriter()) {
                    writer.write("404 - Service not found");
                    response.setStatus(404);
                    writer.flush();
                }
            }
        });

        // Mapping any request to servlet proxy
        ctx.addServletMappingDecoded("/*", "proxy");

        // Start tomcat
        tomcat.getConnector();
        tomcat.start();
        tomcat.getServer().await();
    }
}