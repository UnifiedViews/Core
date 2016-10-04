package cz.cuni.mff.xrg.odcs.commons.app.module.osgi.packages;

/**
 * Packages for RDF, without sesame.
 * 
 * @author Petyr
 */
public class rdf {

    /**
     * List of OSGI packages to export. Does not start nor end with separator.
     */
    public static final String PACKAGE_LIST =
            "org.apache.commons.dbcp.cpdsadapter;uses:=\"org.apache.commons.dbcp,javax.naming,javax.sql,org.apache.commons.pool.impl,org.apache.commons.pool,javax.naming.spi\";version=\"1.3\","
                    +
                    "org.apache.commons.dbcp;uses:=\"org.apache.commons.pool.impl,org.apache.commons.pool,javax.sql,javax.naming,javax.naming.spi,org.apache.commons.jocl,org.xml.sax\";version=\"1.3\","
                    +
                    "org.apache.commons.dbcp.managed;uses:=\"org.apache.commons.dbcp,javax.sql,org.apache.commons.pool.impl,org.apache.commons.pool,javax.transaction,javax.transaction.xa\";version=\"1.3\","
                    +
                    "org.apache.commons.dbcp.datasources;uses:=\"javax.sql,org.apache.commons.pool,javax.naming,org.apache.commons.dbcp,javax.naming.spi,org.apache.commons.pool.impl\";version=\"1.3\","
                    +
                    "org.apache.commons.jocl;uses:=\"org.xml.sax.helpers,org.xml.sax\";version=\"1.3\","
                    +
                    "org.apache.commons.httpclient.protocol;uses:=\"org.apache.commons.httpclient.util,org.apache.commons.httpclient,org.apache.commons.httpclient.params,javax.net,javax.net.ssl\";version=\"3.1.0\","
                    +
                    "org.apache.commons.httpclient.auth;uses:=\"org.apache.commons.httpclient.util,org.apache.commons.httpclient,org.apache.commons.logging,org.apache.commons.httpclient.params,org.apache.commons.codec.binary,javax.crypto.spec,javax.crypto\";version=\"3.1.0\","
                    +
                    "org.apache.commons.httpclient.methods;uses:=\"org.apache.commons.httpclient,org.apache.commons.logging,org.apache.commons.httpclient.params,org.apache.commons.httpclient.methods.multipart,org.apache.commons.httpclient.util\";version=\"3.1.0\","
                    +
                    "org.apache.commons.httpclient.cookie;uses:=\"org.apache.commons.httpclient,org.apache.commons.logging,org.apache.commons.httpclient.util\";version=\"3.1.0\","
                    +
                    "org.apache.commons.httpclient.params;uses:=\"org.apache.commons.logging,org.apache.commons.httpclient\";version=\"3.1.0\","
                    +
                    "org.apache.commons.httpclient.methods.multipart;uses:=\"org.apache.commons.logging,org.apache.commons.httpclient.util,org.apache.commons.httpclient.methods,org.apache.commons.httpclient.params\";version=\"3.1.0\","
                    +
                    "org.apache.commons.httpclient.util;uses:=\"org.apache.commons.logging,org.apache.commons.codec.net,org.apache.commons.httpclient,org.apache.commons.codec\";version=\"3.1.0\","
                    +
                    "org.apache.commons.httpclient;uses:=\"org.apache.commons.logging,org.apache.commons.httpclient.params,org.apache.commons.httpclient.util,org.apache.commons.httpclient.protocol,org.apache.commons.httpclient.cookie,org.apache.commons.httpclient.auth,org.apache.commons.codec,org.apache.commons.codec.net\";version=\"3.1.0\","
                    +
                    "org.apache.commons.io.output;uses:=\"org.apache.commons.io.input,org.apache.commons.io\";version=\"1.4.9999\"," +
                    "org.apache.commons.io.monitor;uses:=\"org.apache.commons.io.comparator,org.apache.commons.io\";version=\"2.4\"," +
                    "org.apache.commons.io.filefilter;uses:=\"org.apache.commons.io\";version=\"1.4.9999\"," +
                    "org.apache.commons.io.comparator;uses:=\"org.apache.commons.io\";version=\"1.4.9999\"," +
                    "org.apache.commons.io.input;uses:=\"org.apache.commons.io\";version=\"1.4.9999\"," +
                    "org.apache.commons.io;uses:=\"org.apache.commons.io.filefilter,org.apache.commons.io.output\";version=\"1.4.9999\"," +
                    "org.apache.commons.pool.impl;uses:=\"org.apache.commons.pool\";version=\"1.5.4\"," +
                    "org.apache.commons.pool;version=\"1.5.4\"," +
                    "au.com.bytecode.opencsv;version=\"2.0.0\"," +
                    "au.com.bytecode.opencsv.bean;uses:=\"au.com.bytecode.opencsv\";version=\"2.0.0\",virtuoso.sesame2.driver";
}
