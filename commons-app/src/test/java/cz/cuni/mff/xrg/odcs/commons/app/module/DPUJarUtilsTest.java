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

import static org.junit.Assert.*;

import org.junit.Test;

public class DPUJarUtilsTest {
    static String[] allowedFormats = new String[] {
            "name-1.0.jar",
            "name-1.0.0.jar",
            "name-1.0.0-SNAPSHOT.jar",
            "name-1.0.0-snapshot.jar",
            "name-1.0.0.snapshot.jar",
            "name-1.0.0-BETA.jar",
            "name-1.0.0-xxx.jar",
            "name-1.0.0xxx.jar"
    };

    @Test
    public void testParseNameFromJarName() throws DPUJarNameFormatException {
        
        for (String jarFileName : allowedFormats) {
            assertNotNull(DPUJarUtils.parseNameFromJarName(jarFileName));
        }
        
        // (.+)+-(\\d(.\\d+)*)(-.+)*\\.jar
        assertEquals("name", DPUJarUtils.parseNameFromJarName("name-1.0.1.jar"));
        assertEquals("name", DPUJarUtils.parseNameFromJarName("name-1.0.1-SNAPSHOT.jar"));
        
        try {
            assertEquals("name", DPUJarUtils.parseNameFromJarName("name.jar"));
            fail("Should throw DPUCreateException because of wrong format.");
        } catch (DPUJarNameFormatException e) {
            // ok
        }
        
        try {
            assertEquals("name", DPUJarUtils.parseNameFromJarName("name-1.jar"));
            fail("Should throw DPUCreateException because of wrong format.");
        } catch (DPUJarNameFormatException e) {
            // ok
        }
        
        assertEquals("name", DPUJarUtils.parseNameFromJarName("name-1.0.jar"));
        assertEquals("name", DPUJarUtils.parseNameFromJarName("name-1.0-SNAPSHOT.jar"));
        
        assertEquals("uv-x-name", DPUJarUtils.parseNameFromJarName("uv-x-name-1.0.1-SNAPSHOT.jar"));
        assertEquals("uv-x-name", DPUJarUtils.parseNameFromJarName("uv-x-name-1.0-SNAPSHOT.jar"));
        assertEquals("uv-x-name", DPUJarUtils.parseNameFromJarName("uv-x-name-1.0.1.jar"));
        assertEquals("uv-x-name", DPUJarUtils.parseNameFromJarName("uv-x-name-1.0.jar"));
        
        assertEquals("uv-x-name", DPUJarUtils.parseNameFromJarName("uv-x-name-1.0.0-RC.jar"));
        assertEquals("uv-x-name", DPUJarUtils.parseNameFromJarName("uv-x-name-1.0.0-xxxx.jar"));
        assertEquals("uv-x-name", DPUJarUtils.parseNameFromJarName("uv-x-name-1.0.0xxx.jar"));
        // (-SNAPSHOT)?
    }
    
    @Test
    public void testParseVersionStringFromJarName() throws DPUJarNameFormatException {
        for (String jarName : allowedFormats) {
            assertNotNull(DPUJarUtils.parseVersionStringFromJarName(jarName));
        }
        
        try {
            assertEquals("name", DPUJarUtils.parseVersionStringFromJarName("name.jar"));
            fail("Should throw DPUCreateException because of wrong format.");
        } catch (DPUJarNameFormatException e) {
            // ok
        }
        
        assertEquals("1.0", DPUJarUtils.parseVersionStringFromJarName("name-1.0.jar"));
        assertEquals("1.0-SNAPSHOT", DPUJarUtils.parseVersionStringFromJarName("name-1.0-SNAPSHOT.jar"));
        assertEquals("1.0-snapshot", DPUJarUtils.parseVersionStringFromJarName("name-1.0-snapshot.jar"));
        assertEquals("1.0.snapshot", DPUJarUtils.parseVersionStringFromJarName("name-1.0.snapshot.jar"));
        assertEquals("1.0-beta", DPUJarUtils.parseVersionStringFromJarName("name-1.0-beta.jar"));
        
        assertEquals("1.0.1-SNAPSHOT", DPUJarUtils.parseVersionStringFromJarName("uv-x-name-1.0.1-SNAPSHOT.jar"));
        assertEquals("1.0.1", DPUJarUtils.parseVersionStringFromJarName("uv-x-name-1.0.1.jar"));
        
        assertEquals("1.0.0-RC", DPUJarUtils.parseVersionStringFromJarName("uv-x-name-1.0.0-RC.jar"));
        assertEquals("1.0.0", DPUJarUtils.parseVersionStringFromJarName("uv-x-name-1.0.0-xxxx.jar"));
        assertEquals("1.0.0", DPUJarUtils.parseVersionStringFromJarName("uv-x-name-1.0.0xxx.jar"));
    }
    
    @Test
    public void testCompareVersions() {
        assertTrue(DPUJarUtils.compareVersions("1.0", "1.0") == 0);
        assertTrue(DPUJarUtils.compareVersions("1.0.0", "1.0.0") == 0);
        assertTrue(DPUJarUtils.compareVersions("1.0", "1.0.0") == 0);
        assertTrue(DPUJarUtils.compareVersions("1.0", "1.00.0") == 0);
        assertTrue(DPUJarUtils.compareVersions("1.00", "1.0.0") == 0);
        assertTrue(DPUJarUtils.compareVersions("1.01.0", "1.1.0") == 0);
        
        assertTrue(DPUJarUtils.compareVersions("1.0.0", "1.0.0-SNAPSHOT") > 0);
        assertTrue(DPUJarUtils.compareVersions("1.0.0", "1.0.0-snapshot") > 0);
        assertTrue(DPUJarUtils.compareVersions("1.0.0", "1.0.1-SNAPSHOT") < 0);
        
        assertTrue(DPUJarUtils.compareVersions("1.0.0", "1.0.0-RC") > 0);
        assertTrue(DPUJarUtils.compareVersions("1.0.0", "1.0.0-rc") > 0);
        
        assertTrue(DPUJarUtils.compareVersions("1.0.0-RC", "1.0.0-SNAPSHOT") < 0); // i am not sure
        
        assertTrue(DPUJarUtils.compareVersions("1.0.0-alfa", "1.0.0-beta") > 0); // i am not sure here either
    }
    
    @Test
    public void testCompareVersionsFromJarName() throws DPUJarNameFormatException {
        assertTrue(DPUJarUtils.compareJarVersionsFromJarName(
                "uv-e-filesFromCKAN-1.1.0-SNAPSHOT.jar",
                "uv-e-filesFromCKAN-1.0.0.jar") > 0);
        assertTrue(DPUJarUtils.compareJarVersionsFromJarName(
                "uv-e-filesFromCKAN-1.0.0.jar",
                "uv-e-filesFromCKAN-1.1.0-SNAPSHOT.jar") < 0);
        assertTrue(DPUJarUtils.compareJarVersionsFromJarName(
                "uv-e-filesFromCKAN-1.0.0-SNAPSHOT.jar",
                "uv-e-filesFromCKAN-1.0.0.jar") < 0);
        assertTrue(DPUJarUtils.compareJarVersionsFromJarName(
                "uv-e-filesFromCKAN-1.0.0.jar",
                "uv-e-filesFromCKAN-1.0.0.jar") == 0);
    }
}
