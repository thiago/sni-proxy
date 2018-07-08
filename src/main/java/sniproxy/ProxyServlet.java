package sniproxy;

import org.apache.http.HttpHost;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


public class ProxyServlet extends HttpServlet {
    private Configuration configs;
    private Map<String, HttpServlet> routes = new HashMap<>();

    ProxyServlet(Configuration configs) {
        this.configs = configs;
    }

    private HttpServlet getRoute(String fqdn) {
        if (routes.get(fqdn) != null) {
            return routes.get(fqdn);
        }

        Route route = configs.getRoute(fqdn);
        if (route == null) {
            return null;
        }

        CustomProxyServlet proxy = new CustomProxyServlet();
        try {
            proxy.init(route, this.getServletConfig());
        } catch (ServletException e) {
            e.printStackTrace();
            return null;
        }
        routes.put(fqdn, proxy);
        return proxy;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String defaultMessage = "404 - Service not found";
        int defaultStatus = 404;
        HttpServlet proxy = getRoute(request.getServerName());
        if (proxy != null) {
            try {
                proxy.service(request, response);
                return;
            } catch (ServletException e) {
                e.printStackTrace();
                defaultMessage = "500 - " + e.getMessage();
                defaultStatus = 500;
            } catch (org.apache.http.NoHttpResponseException e) {
                defaultMessage = "503";
                defaultStatus = 503;
            }
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        try (Writer writer = response.getWriter()) {
            writer.write(defaultMessage);
            response.setStatus(defaultStatus);
            writer.flush();
        }
    }
}

class CustomProxyServlet extends org.mitre.dsmiley.httpproxy.ProxyServlet {
    private Route route;

    @Override
    protected String getConfigParam(String key) {
        switch (key) {
            case "targetUri":
                return route.getNextTarget();
            case "forwardip":
                return route.getForwardIP().toString();
            case "preserveHost":
                return route.getPreserveHost().toString();
            case "preserveCookies":
                return route.getPreserveCookies().toString();
            default:
                return super.getConfigParam(key);
        }
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        HttpHost host = this.route.getNextTargetHost();
        if (servletRequest.getAttribute(ATTR_TARGET_URI) == null) {
            servletRequest.setAttribute(ATTR_TARGET_URI, host.toString());
        }

        if (servletRequest.getAttribute(ATTR_TARGET_HOST) == null) {
            servletRequest.setAttribute(ATTR_TARGET_HOST, host);
        }
        super.service(servletRequest, servletResponse);
    }

    void init(Route route, ServletConfig config) throws ServletException {
        this.route = route;
        super.init(config);
    }
}
