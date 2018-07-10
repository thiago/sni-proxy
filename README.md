# Simple SNI Proxy [![Build Status][travis-image]][travis-url]

This is a simple reverse proxy with support to SNI based on FQDN and the alias in Java Keystore

## Features

- Configuration by xml file
- SNI Support - Based on FQDN and the alias in Java Keystore

## Usage

First you need to create a xml configuration file with backend routes.

    vim sniproxy.xml

And paste 

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <!-- Port to listen --> 
    <port>8443</port>
    <sni>
        <!-- The path to keytore file -->
        <file>keys.jks</file>
        <!-- Provide password to keystore if there need -->
        <password>password</password>
        <!-- 
        Provide alias to a default certificate if fqdn does not match with any alias. 
        This is optional. The default is the first alias
         -->
        <alias>test0.example.com</alias>
    </sni>
   
    <routes>
        <route>
            <fqdn>test1.example.com</fqdn>
            <targets>
                <target>https://status.github.com</target>
            </targets>
        </route>
        <route>
            <fqdn>test2.example.com</fqdn>
            <targets>
                <target>http://localhost:9091</target>
                <target>http://localhost:9092</target>
            </targets>
        </route>
    </routes>
</configuration>
```

### Creating a Keystore

The command `./keystore.sh` creates a file called `keys.jks` protected by `password` with 4 certificates
 (`test0.example.com`, `test1.example.com`, `test2.example.com` and `test3.example.com`)

    ./keystore.sh

To list certificates :

    keytool -list -keystore keys.jks -storepass password

## Running with Docker

    docker run -ti \
        -p 8443:8443 \
        -v $(pwd)/sniproxy.xml:/opt/app/sniproxy.xml \
        -v $(pwd)/keys.jks:/opt/app/keys.jks \
        -e CONFIG_FILE=/opt/app/sniproxy.xml \
        trsouz/sni-proxy


## Running with jar

**Download the latest .jar**

    VERSION=`curl -sLo /dev/null -w '%{url_effective}' https://github.com/thiago/sni-proxy/releases/latest | rev | awk -F '/' '{print $1}' | rev`
    curl -sL -o sniproxy.jar https://github.com/thiago/sni-proxy/releases/download/${VERSION}/sniproxy.jar

**Then execute**
    
    java -Dconfig=sniproxy.xml -jar sniproxy.jar


### Testing

    # check reverse proxy
    curl https://localhost:8443/api/status.json -k -H 'host: test1.example.com'
    
    # check sni support
    openssl s_client -connect localhost:8443 -servername test1.example.com 2>&1 | grep "subject=" 


[travis-image]: https://travis-ci.org/thiago/sni-proxy.svg?branch=master
[travis-url]: https://travis-ci.org/thiago/sni-proxy