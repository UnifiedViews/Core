package cz.cuni.mff.xrg.odcs.commons.app.module.osgi.packages;

/**
 * The package list for commons-web without Vaadin. The commons
 * module can be exported only from frontend.
 * 
 * @author Petyr
 */
public class commons_web {

    /**
     * List of OSGI packages to export. Does not start nor end with separator.
     */
    public static final String PACKAGE_LIST =
            "com.steadystate.css.userdata;version=\"0.9.5\"," +
                    "com.steadystate.css.parser.selectors;uses:=\"org.w3c.css.sac\";version=\"0.9.5\"," +
                    "com.steadystate.css.sac;uses:=\"org.w3c.css.sac\";version=\"0.9.5\"," +
                    // com.steadystate.css.userdata, org.w3c.dom.stylesheets
                    "com.steadystate.css.parser;uses:=\"com.steadystate.css.parser.selectors,com.steadystate.css.sac,org.w3c.css.sac,org.w3c.dom.css,org.w3c.dom,com.steadystate.css.dom,org.w3c.css.sac.helpers\";version=\"0.9.5\"," +
                    "com.steadystate.css.dom;uses:=\"org.w3c.dom.css,org.w3c.dom,org.w3c.css.sac,com.steadystate.css.parser,org.w3c.dom.stylesheets,com.steadystate.css.userdata\";version=\"0.9.5\"," +
                    "org.apache.commons.cli;version=\"1.2\"," +
                    "org.apache.commons.jexl2.internal.introspection;uses:=\"org.apache.commons.logging\";version=\"2.1.1\"," +
                    "org.apache.commons.jexl2.internal;uses:=\"org.apache.commons.jexl2.introspection,org.apache.commons.jexl2.internal.introspection,org.apache.commons.logging\";version=\"2.1.1\"," +
                    "org.apache.commons.jexl2.introspection;uses:=\"org.apache.commons.jexl2.internal.introspection,org.apache.commons.logging,org.apache.commons.jexl2,org.apache.commons.jexl2.internal\";version=\"2.1.1\"," +
                    "org.apache.commons.jexl2.parser;uses:=\"org.apache.commons.jexl2\";version=\"2.1.1\"," +
                    "org.apache.commons.jexl2.scripting;uses:=\"org.apache.commons.logging,org.apache.commons.jexl2.introspection,org.apache.commons.jexl2,javax.script,org.apache.commons.jexl2.parser\";version=\"2.1.1\"," +
                    "org.apache.commons.jexl2;uses:=\"org.apache.commons.jexl2.parser,org.apache.commons.logging,org.apache.commons.jexl2.introspection\";version=\"2.1.1\"," +
                    // org.apache.avalon.framework.logger,
                    "org.apache.commons.logging.impl;uses:=\"org.apache.commons.logging,org.apache.log4j,org.apache.log,javax.servlet\";version=\"1.1.1\"," +
                    "org.apache.commons.logging;uses:=\"org.apache.commons.logging.impl\";version=\"1.1.1\"," +
                    "org.json;version=\"0.0.20080701\"," +
                    "org.jsoup;uses:=\"org.jsoup.parser,org.jsoup.nodes,org.jsoup.helper,org.jsoup.safety\";version=\"1.6.3\"," +
                    "org.jsoup.examples;uses:=\"org.jsoup.select,org.jsoup.nodes,org.jsoup.helper,org.jsoup\";version=\"1.6.3\"," +
                    "org.jsoup.helper;uses:=\"org.jsoup.parser,org.jsoup.select,org.jsoup.nodes,org.jsoup\";version=\"1.6.3\"," +
                    "org.jsoup.nodes;uses:=\"org.jsoup.helper,org.jsoup.parser,org.jsoup.select\";version=\"1.6.3\"," +
                    "org.jsoup.parser;uses:=\"org.jsoup.helper,org.jsoup.nodes\";version=\"1.6.3\"," +
                    "org.jsoup.safety;uses:=\"org.jsoup.nodes,org.jsoup.parser,org.jsoup.helper\";version=\"1.6.3\"," +
                    "org.jsoup.select;uses:=\"org.jsoup.nodes,org.jsoup.helper,org.jsoup.parser\";version=\"1.6.3\"," +
                    // added lines
                    "org.w3c.css.sac;version=\"1.3.0\"," +
                    "org.w3c.css.sac.helpers;uses:=\"org.w3c.css.sac\";version=\"1.3.0\"," +
                    "javax.servlet;version=\"2.4.0\"," +
                    "javax.servlet.http;uses:=\"javax.servlet\";version=\"2.4.0\"," +
                    "javax.servlet.resources;version=\"2.4.0\"," +
                    // added, used gwt dependencies originally
                    "javax.validation;version=\"1.0.0.GA\"," +
                    "javax.validation.bootstrap;uses:=\"javax.validation\";version=\"1.0.0.GA\"," +
                    "javax.validation.constraints;uses:=\"javax.validation\";version=\"1.0.0.GA\"," +
                    "javax.validation.metadata;uses:=\"javax.validation\";version=\"1.0.0.GA\"," +
                    "javax.validation.spi;uses:=\"javax.validation\";version=\"1.0.0.GA\"," +
                    "javax.validation;uses:=\"javax.validation.metadata,javax.validation.spi,javax.validation.bootstrap\";version=\"1.0.0.GA\"," +
                    // 
                    "org.w3c.flute.parser.selectors;uses:=\"org.w3c.css.sac\";version=\"1.3.0.gg2\"," +
                    "org.w3c.flute.parser;uses:=\"org.w3c.css.sac,org.w3c.flute.util,org.w3c.flute.parser.selectors\";version=\"1.3.0.gg2\"," +
                    "org.w3c.flute.util;version=\"1.3.0.gg2\"";

}
