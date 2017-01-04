package cz.cuni.mff.xrg.odcs.commons.app.module.osgi.packages;

/**
 * The list of packages exported for commons-module.\
 * The xStream dependency is in commons_module, but it's presented
 * in backend/frontend so we exposed it.
 * 
 * @author Petyr
 */
public class commons_module {

    /**
     * List of OSGI packages to export. Does not start nor end with separator.
     */
    public static final String PACKAGE_LIST =
            // on line can be the list or packages removeed rom the following line definition
            "com.thoughtworks.xstream.annotations;uses:=\"com.thoughtworks.xstream.converters,com.thoughtworks.xstream.converters.reflection,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.converters.basic;uses:=\"com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io,com.thoughtworks.xstream.core,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.mapper\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.converters.collections;uses:=\"com.thoughtworks.xstream.io,com.thoughtworks.xstream.converters,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.core\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.converters.enums;uses:=\"com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream.converters.collections,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.converters.basic\";version=\"1.4.4\","
                    +
                    // org.joda.time,org.joda.time.format,
                    "com.thoughtworks.xstream.converters.extended;uses:=\"com.thoughtworks.xstream.converters.basic,com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io,javax.xml.datatype,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream.core.util,javax.swing.plaf,com.thoughtworks.xstream.converters.reflection,javax.swing,com.thoughtworks.xstream.converters.collections,javax.security.auth\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.converters.javabean;uses:=\"com.thoughtworks.xstream.converters.reflection,com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.core\";version=\"1.4.4\","
                    +
                    // net.sf.cglib.proxy,sun.reflect,sun.misc
                    "com.thoughtworks.xstream.converters.reflection;uses:=\"com.thoughtworks.xstream.converters.basic,com.thoughtworks.xstream.io,com.thoughtworks.xstream.converters,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream.core,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.converters;uses:=\"com.thoughtworks.xstream,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.io\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.core.util;uses:=\"com.thoughtworks.xstream.converters.reflection,com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io,com.thoughtworks.xstream.mapper\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.core;uses:=\"com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io.path,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.io,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream,com.thoughtworks.xstream.converters.reflection\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.io.binary;uses:=\"com.thoughtworks.xstream.io,com.thoughtworks.xstream.converters\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.io.copy;uses:=\"com.thoughtworks.xstream.io\";version=\"1.4.4\","
                    +
                    // org.codehaus.jettison.mapped,org.codehaus.jettison,
                    "com.thoughtworks.xstream.io.json;uses:=\"com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream.io.naming,com.thoughtworks.xstream.core.util,javax.xml.stream,com.thoughtworks.xstream.io.xml,javax.xml.namespace\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.io.naming;version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.io.path;uses:=\"com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.io.xml.xppdom;uses:=\"org.xmlpull.mxp1,org.xmlpull.v1\";version=\"1.4.4\","
                    +
                    // com.bea.xml.stream,org.dom4j,org.dom4j.io,org.dom4j.tree,org.jdom.input,org.jdom,org.kxml2.io,nu.xom,com.ctc.wstx.stax,
                    "com.thoughtworks.xstream.io.xml;uses:=\"com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io,com.thoughtworks.xstream.io.naming,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.io.xml.xppdom,org.xmlpull.v1,javax.xml.stream,org.xml.sax.helpers,org.xml.sax,javax.xml.parsers,org.w3c.dom,javax.xml.namespace,com.thoughtworks.xstream,javax.xml.transform,javax.xml.transform.stream,javax.xml.transform.sax,com.thoughtworks.xstream.converters.reflection,org.xmlpull.mxp1\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.io;uses:=\"com.thoughtworks.xstream.io.naming,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.converters,com.thoughtworks.xstream\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.mapper;uses:=\"com.thoughtworks.xstream.annotations,com.thoughtworks.xstream.converters,com.thoughtworks.xstream.converters.reflection,com.thoughtworks.xstream,com.thoughtworks.xstream.core,com.thoughtworks.xstream.core.util,net.sf.cglib.proxy,com.thoughtworks.xstream.converters.enums\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream.persistence;uses:=\"com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream,com.thoughtworks.xstream.io.xml\";version=\"1.4.4\","
                    +
                    "com.thoughtworks.xstream;uses:=\"com.thoughtworks.xstream.converters,com.thoughtworks.xstream.io,com.thoughtworks.xstream.mapper,com.thoughtworks.xstream.core.util,com.thoughtworks.xstream.converters.basic,com.thoughtworks.xstream.converters.extended,com.thoughtworks.xstream.converters.reflection,com.thoughtworks.xstream.io.xml,com.thoughtworks.xstream.converters.collections,com.thoughtworks.xstream.core\";version=\"1.4.4\","
                    +
                    "org.xmlpull.mxp1;uses:=\"org.xmlpull.v1\";version=\"1.1.0.4c\"," +
                    "org.xmlpull.v1;version=\"1.1.0.4c\"," +
                    "org.xmlpull.v1;version=\"1.1.3.1\"";

}
