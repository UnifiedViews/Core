package cz.cuni.mff.xrg.odcs.commons.app.module.osgi.packages;

/**
 * The list of packages exported from commons.
 * 
 * @author Petyr
 */
public class commons {

    /**
     * List of OSGI packages to export. Does not start nor end with separator.
     */
    public static final String PACKAGE_LIST =
            "org.slf4j.bridge;uses:=\"org.slf4j.spi,org.slf4j\";version=\"1.7.7\"," +
                    "org.apache.log4j;uses:=\"org.apache.log4j.spi,org.slf4j.spi,org.apache.log4j.helpers,org.slf4j,org.slf4j.helpers\";version=\"1.2.17\"," +
                    "org.apache.log4j.helpers;version=\"1.2.17\"," +
                    "org.apache.log4j.xml;uses:=\"javax.xml.parsers,org.apache.log4j.spi,org.w3c.dom\";version=\"1.2.17\"," +
                    "org.apache.log4j.spi;uses:=\"org.apache.log4j\";version=\"1.2.17\"," +
                    "org.slf4j;version=\"1.7.7\"," + // Added
                    "org.slf4j.spi;uses:=\"org.slf4j\";version=\"1.7.7\"," +
                    "org.slf4j.helpers;uses:=\"org.slf4j.spi,org.slf4j\";version=\"1.7.7\"," +
                    "org.slf4j;uses:=\"org.slf4j.helpers,org.slf4j.spi\";version=\"1.7.7\"";

}
