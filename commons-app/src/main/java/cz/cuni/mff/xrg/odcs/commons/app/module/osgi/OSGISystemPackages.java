/*******************************************************************************
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/*******************************************************************************
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package cz.cuni.mff.xrg.odcs.commons.app.module.osgi;

/**
 * List of believed system packages.
 * 
 * @author Petyr
 */
class OSGISystemPackages {

    /**
     * List of OSGI packages to export. Does not start nor end with separator.
     */
    public static final String PACKAGES = "javax.accessibility,"
            + "javax.activation,"
            + "javax.activity,"
            + "javax.annotation,"
            + "javax.annotation.processing,"
            + "javax.crypto,"
            + "javax.crypto.interfaces,"
            + "javax.crypto.spec,"
            + "javax.imageio,"
            + "javax.imageio.event,"
            + "javax.imageio.metadata,"
            + "javax.imageio.plugins.bmp,"
            + "javax.imageio.plugins.jpeg,"
            + "javax.imageio.spi,"
            + "javax.imageio.stream,"
            + "javax.jws,"
            + "javax.jws.soap,"
            + "javax.lang.model,"
            + "javax.lang.model.element,"
            + "javax.lang.model.type,"
            + "javax.lang.model.util,"
            + "javax.management,"
            + "javax.management.loading,"
            + "javax.management.modelmbean,"
            + "javax.management.monitor,"
            + "javax.management.openmbean,"
            + "javax.management.relation,"
            + "javax.management.remote,"
            + "javax.management.remote.rmi,"
            + "javax.management.timer,"
            + "javax.naming,"
            + "javax.naming.directory,"
            + "javax.naming.event,"
            + "javax.naming.ldap,"
            + "javax.naming.spi,"
            + "javax.net,"
            + "javax.net.ssl,"
            + "javax.print,"
            + "javax.print.attribute,"
            + "javax.print.attribute.standard,"
            + "javax.print.event,"
            + "javax.rmi,"
            + "javax.rmi.CORBA,"
            + "javax.rmi.ssl,"
            + "javax.script,"
            + "javax.security.auth,"
            + "javax.security.auth.callback,"
            + "javax.security.auth.kerberos,"
            + "javax.security.auth.login,"
            + "javax.security.auth.spi,"
            + "javax.security.auth.x500,"
            + "javax.security.cert,"
            + "javax.security.sasl,"
            + "javax.security.jacc,"
            + "javax.sound.midi,"
            + "javax.sound.midi.spi,"
            + "javax.sound.sampled,"
            + "javax.sound.sampled.spi,"
            + "javax.sql,"
            + "javax.sql.rowset,"
            + "javax.sql.rowset.serial,"
            + "javax.sql.rowset.spi,"
            + "javax.swing,"
            + "javax.swing.border,"
            + "javax.swing.colorchooser,"
            + "javax.swing.event,"
            + "javax.swing.filechooser,"
            + "javax.swing.plaf,"
            + "javax.swing.plaf.basic,"
            + "javax.swing.plaf.metal,"
            + "javax.swing.plaf.multi,"
            + "javax.swing.plaf.nimbus,"
            + "javax.swing.plaf.synth,"
            + "javax.swing.table,"
            + "javax.swing.text,"
            + "javax.swing.text.html,"
            + "javax.swing.text.html.parser,"
            + "javax.swing.text.rtf,"
            + "javax.swing.tree,"
            + "javax.swing.undo,"
            + "javax.tools,"
            + "javax.transaction,"
            + "javax.transaction.xa,"
            + "javax.xml,"
            + "javax.xml.bind,"
            + "javax.xml.bind.annotation,"
            + "javax.xml.bind.annotation.adapters,"
            + "javax.xml.bind.attachment,"
            + "javax.xml.bind.helpers,"
            + "javax.xml.bind.util,"
            + "javax.xml.crypto,"
            + "javax.xml.crypto.dom,"
            + "javax.xml.crypto.dsig,"
            + "javax.xml.crypto.dsig.dom,"
            + "javax.xml.crypto.dsig.keyinfo,"
            + "javax.xml.crypto.dsig.spec,"
            + "javax.xml.datatype,"
            + "javax.xml.namespace,"
            + "javax.xml.parsers,"
            + "javax.xml.soap,"
            + "javax.xml.stream,"
            + "javax.xml.stream.events,"
            + "javax.xml.stream.util,"
            + "javax.xml.transform,"
            + "javax.xml.transform.dom,"
            + "javax.xml.transform.sax,"
            + "javax.xml.transform.stax,"
            + "javax.xml.transform.stream,"
            + "javax.xml.validation,"
            + "javax.xml.ws,"
            + "javax.xml.ws.handler,"
            + "javax.xml.ws.handler.soap,"
            + "javax.xml.ws.http,"
            + "javax.xml.ws.soap,"
            + "javax.xml.ws.spi,"
            + "javax.xml.ws.spi.http,"
            + "javax.xml.ws.wsaddressing,"
            + "javax.xml.xpath,"
            + "javax.enterprise.context,"
            + "javax.enterprise.util,"
            + "javax.interceptor,"
            + "javax.validation,"
            + "javax.validation.constraints,"
            + "javax.validation.groups,"
            + "javax.validation.metadata,"
            + "javax.resource,"
            + "javax.resource.spi,"
            + "javax.resource.spi.endpoint,"
            + "javax.resource.spi.security,"
            + "org.ietf.jgss,"
            + "org.omg.CORBA,"
            + "org.omg.CORBA_2_3,"
            + "org.omg.CORBA_2_3.portable,"
            + "org.omg.CORBA.DynAnyPackage,"
            + "org.omg.CORBA.ORBPackage,"
            + "org.omg.CORBA.portable,"
            + "org.omg.CORBA.TypeCodePackage,"
            + "org.omg.CosNaming,"
            + "org.omg.CosNaming.NamingContextExtPackage,"
            + "org.omg.CosNaming.NamingContextPackage,"
            + "org.omg.Dynamic,"
            + "org.omg.DynamicAny,"
            + "org.omg.DynamicAny.DynAnyFactoryPackage,"
            + "org.omg.DynamicAny.DynAnyPackage,"
            + "org.omg.IOP,"
            + "org.omg.IOP.CodecFactoryPackage,"
            + "org.omg.IOP.CodecPackage,"
            + "org.omg.Messaging,"
            + "org.omg.PortableInterceptor,"
            + "org.omg.PortableInterceptor.ORBInitInfoPackage,"
            + "org.omg.PortableServer,"
            + "org.omg.PortableServer.CurrentPackage,"
            + "org.omg.PortableServer.POAManagerPackage,"
            + "org.omg.PortableServer.POAPackage,"
            + "org.omg.PortableServer.portable,"
            + "org.omg.PortableServer.ServantLocatorPackage,"
            + "org.omg.SendingContext,"
            + "org.omg.stub.java.rmi,"
            + "org.w3c.dom;version=\"3.0.0\","
            + "org.w3c.dom.bootstrap,"
            + "org.w3c.dom.events,"
            + "org.w3c.dom.ls,"
            + "org.xml.sax;version=\"2.0.2\","
            + "org.xml.sax.ext;version=\"2.0.2\","
            + "org.xml.sax.helpers";

}
