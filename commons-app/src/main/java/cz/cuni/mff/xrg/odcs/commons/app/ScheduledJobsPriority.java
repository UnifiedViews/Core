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

/**
 * Priorities for the scheduled events
 * 
 * @author tomasknap
 */
public enum ScheduledJobsPriority {
    IGNORE(0), HIGHEST(3), MEDIUM(2), LOWEST(1);

    private final long priority;

    ScheduledJobsPriority(long priority) {
        this.priority = priority;
    }

    public long getValue() {
        return priority;
    }

    public static ScheduledJobsPriority getForValue(final long value) {
        if (value <= 0) {
            return IGNORE;
        } else if (value == MEDIUM.getValue()) {
            return MEDIUM;
        } else if (value == LOWEST.getValue()) {
            return LOWEST;
        }
        return HIGHEST; // value >= HIGHEST
    }
}
