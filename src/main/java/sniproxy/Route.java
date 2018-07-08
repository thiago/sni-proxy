package sniproxy;

import java.util.ArrayList;

import org.apache.http.HttpHost;

import java.util.Iterator;
import java.util.List;

class Route {
    private String fqdn;
    private List<HttpHost> targets;
    private Iterator<HttpHost> targetsIterator;
    private Boolean forwardIP;
    private Boolean preserveHost;
    private Boolean preserveCookies;

    Route(String fqnd, String... targets) {
        this.fqdn = fqnd;
        this.targets = new ArrayList<>();
        for (String target : targets) {
            this.targets.add(HttpHost.create(target));
        }
        targetsIterator = this.targets.iterator();
    }

    String getFqdn() {
        return fqdn;
    }

    String getNextTarget() {
        return getNextTargetHost().toString();
    }

    HttpHost getNextTargetHost() {
        if (!targetsIterator.hasNext()) {
            targetsIterator = targets.iterator();
        }
        return targetsIterator.next();
    }

    Boolean getForwardIP() {
        return forwardIP;
    }

    Boolean getPreserveHost() {
        return preserveHost;
    }

    Boolean getPreserveCookies() {
        return preserveCookies;
    }

    void setForwardIP(Boolean forwardIP) {
        this.forwardIP = forwardIP;
    }

    void setPreserveHost(Boolean preserveHost) {
        this.preserveHost = preserveHost;
    }

    void setPreserveCookies(Boolean preserveCookies) {
        this.preserveCookies = preserveCookies;
    }
}