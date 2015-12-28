/**
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
 */
package cz.cuni.mff.xrg.odcs.commons.app.module;

import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DPUJarUtils {

    /**
     * <p>
     * DPU's should be using maven "bundle" packaging type, the version is put in this attribute
     * within it's manifest.
     * <p>
     * Value examples:<br/>
     * 1.0.0<br/>
     * 1.0.0-SNAPSHOT
     * 
     */
    public static final String MANIFEST_ATTR_BUNDLE_VERSION = "Bundle-Version";
    
    /**
     * DPU's should be using maven "bundle" packaging type, the name is put in this attribute
     * within it's manifest. This manifest corresponds to DPU name, e.g. uv-e-DPUNAME
     */
    public static final String MANIFEST_ATTR_BUNDLE_NAME = "Bundle-Name";

    /**
     * Possible version qualifiers
     */
    private static final String[] QUALIFIERS = { "snapshot", "alpha", "beta", "milestone", "rc", "sp" };

    
    /**
     * Gets version string
     * 
     * @param jarFile
     *          jar file
     * @return
     *          bundle version if found, null otherwise
     * @throws IOException
     */
    public static String getVersionFromJarManifest(File jarFile) throws IOException {
        Attributes attributes = getMainAttributes(jarFile);
        if (attributes.getValue(MANIFEST_ATTR_BUNDLE_VERSION) != null) {
            return attributes.getValue(MANIFEST_ATTR_BUNDLE_VERSION);
        } else {
            return null; // TODO throw exception?
        }
    }
    
    /**
     * Gets main attribute value from jar manifest 
     * 
     * @param jarFile
     *          jar file
     * @param key
     *          name of attribute we are looking for
     * @return
     *          value of the attribute, null if not found
     * @throws IOException
     */
    public static String getManifestValue(File jarFile, String key) throws IOException {
        JarFile jF = null;
        try {
            jF = new JarFile(jarFile);
            Manifest mf = jF.getManifest();
            return mf.getMainAttributes().getValue(key);
        } finally {
            if (jF != null) {
                jF.close();
            }
        }
    }
    
    /**
     * Gets all main attributes of jar manifest
     * 
     * @param jarFile
     * @return
     * @throws IOException
     */
    private static Attributes getMainAttributes(File jarFile) throws IOException {
        JarFile jF = null;
        try {
            jF = new JarFile(jarFile);
            Manifest mf = jF.getManifest();
            return mf.getMainAttributes();
        } finally {
            if (jF != null) {
                jF.close();
            }
        }
    }
    
    /**
     * Compares version from manifest of the given jar files
     * 
     * 
     * @param jar1
     * @param jar2
     * @return -1 if v1 < v2,
     *          1 if v1 > v2,
     *          0 if v1 == v2
     * @throws IOException
     */
    public static int compareJarVersionsFromManifest(File jar1, File jar2) throws IOException {
        return compareVersions(getVersionFromJarManifest(jar1), getVersionFromJarManifest(jar2));
    }
    
    /**
     * Compares two version strings
     * 
     * @param v1String
     * @param v2String
     * @return -1 if v1String < v2String,q
     *          1 if v1String > v2String,
     *          0 if v1String == v2String 
     */
    public static int compareVersions(String v1String, String v2String) {
        ComparableVersion v1 = new ComparableVersion(v1String);
        ComparableVersion v2 = new ComparableVersion(v2String);
        
        return v1.compareTo(v2);
    }
    
    /**
     * Compares version from jar name
     * 
     * 
     * @param jar1
     *      dpu jar file name, e.g. name-version.jar
     * @param jar2
     *      dpu jar file name, e.g. name-version.jar
     * @return -1 if v1 < v2,
     *          1 if v1 > v2,
     *          0 if v1 == v2
     * @throws DPUJarNameFormatException 
     */
    public static int compareJarVersionsFromJarName(String jar1, String jar2) throws DPUJarNameFormatException {
        return compareVersions(parseVersionStringFromJarName(jar1), parseVersionStringFromJarName(jar2));
    }
    
    /**
     * <p>
     * Parses DPU name from jar name. If the format doesn't correspond DPU jar name convention,
     * exception is thrown.
     * </p>
     * name-1.0.0.jar -> name
     * 
     * @param sourceFileName
     *            Name of DPU's jar file.
     * @return
     *            Name of the DPU
     * @throws DPUJarNameFormatException
     */
    public static String parseNameFromJarName(String sourceFileName) throws DPUJarNameFormatException {
        // the name must be in format: NAME-.*.jar
        final Pattern pattern = Pattern.compile(getDPUJarNamePatternString());
        final Matcher matcher = pattern.matcher(sourceFileName);
        if (matcher.matches()) {
            // 0 - original, 1 - name, 2 - version
            return matcher.group(1);
        } else {
            throw new DPUJarNameFormatException(Messages.getString("DPUJarUtils.dpu.name.wrong.format", sourceFileName)); // TODO
        }
    }
    
    /**
     * Parses version string from dpu jar name
     * <br/><br/>
     * name-1.0.0.jar -> 1.0.0<br/>
     * name-1.0.0-snapshot.jar -> 1.0.0-snapshot<br/>
     * name-1.0.0-SNAPSHOT.jar -> 1.0.0-SNAPSHOT<br/>
     * name-1.0.0-20150728.114515-9.jar -> 1.0.0<br/>
     * name-1.0.0-rc.jar -> 1.0.0-rc<br/>
     * name.jar -> exception
     * 
     * @param sourceFileName
     *          Name of DPU's jar file.
     * @return
     *          Version string
     * @throws DPUJarNameFormatException
     *          if the DPU jar name is wrong
     */
    public static String parseVersionStringFromJarName(String sourceFileName) throws DPUJarNameFormatException {
        // needs to be case insensitive because of qualifiers
        final Pattern pattern = Pattern.compile(getDPUJarNamePatternString(), Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(sourceFileName);
        if (matcher.matches()) {
            // 0 - original, 1 - name, 2 - version
            return matcher.group(2);
        } else {
            throw new DPUJarNameFormatException(Messages.getString("DPUJarUtils.dpu.name.wrong.format", sourceFileName)); // TODO
        }
    }
    
    /**
     * Creates regex pattern to parse DPU name and version from jar name
     * <br/><br/>
     * group 0 -> original string<br/>
     * group 1 -> DPU name<br/>
     * group 2 -> DPU version<br/>
     * 
     * @return
     */
    private static String getDPUJarNamePatternString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(.+)-(\\d(\\.\\d+)+");
        sb.append("([.-]");
        sb.append("[(").append(StringUtils.join(QUALIFIERS, ")(")).append(")]+");
        sb.append(")?)").append(".*\\.jar");
        
        return sb.toString();
    }
}
