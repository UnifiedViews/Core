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
package cz.cuni.mff.xrg.odcs.commons.app;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.tools.profiler.PerformanceProfiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used when it is necessary to have detailed information about the way
 * how JPA creates queries to work with database
 * To turn the detailed logging on, change L73 to "true", otherwise this class has no effect
 * 
 * @author Škoda Petr
 */
public class PerformanceSessionCustomizer implements SessionCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceSessionCustomizer.class);

    public static class LogWriter extends Writer {

        @Override
        public void write(String str) throws IOException {
            // This method is used by PerformanceProfiler to log data.
            if (StringUtils.isNotBlank(str)) {
                str = str.replaceAll("\\r|\\n", "");
                LOG.debug(str);
            }
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            // not used
        }

        @Override
        public void flush() throws IOException {
            // no-op
        }

        @Override
        public void close() throws IOException {
            // no-op
        }

    }

    @Override
    public void customize(Session session) throws Exception {
        // Set writer for logs - times are in nano seconds!! (10^-9)

        //if (System.getProperty("eclipseLink.log") != null) {
        if (false)
        {
            session.setLog(new LogWriter());

            // https://docs.oracle.com/middleware/1212/toplink/TLADG/performance.htm#TLADG446
            // http://wiki.eclipse.org/EclipseLink/UserGuide/JPA/Advanced_JPA_Development/Performance/Performance_Monitoring_and_Profiling/Performance_Profiling
            final PerformanceProfiler profiler = new PerformanceProfiler();
            session.setProfiler(profiler);
        }

        // Monitor for group fetching
        // Enable this monitor using the System property org.eclipse.persistence.fetchgroupmonitor=true.

        // QueryMonitor
        // The monitor dumps the number of query cache hits and executions (misses) once every 100 seconds.
    }

}
