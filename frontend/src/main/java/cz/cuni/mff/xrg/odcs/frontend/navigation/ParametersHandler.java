package cz.cuni.mff.xrg.odcs.frontend.navigation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.tepi.filtertable.datefilter.DateInterval;
import org.tepi.filtertable.numberfilter.NumberInterval;

/**
 * Class for holding filter parameters and showing them in the URL.
 * 
 * @author Bogo
 */
public class ParametersHandler {

    /**
     * Get interval from string value.
     * 
     * @param value
     *            Interval value represented as string.
     * @return Interval.
     */
    public static NumberInterval getInterval(String value) {
        if (value.contains("-")) {
            String[] boundaries = value.split("-", -1);
            return new NumberInterval(boundaries[1].isEmpty() ? null : boundaries[1], boundaries[0].isEmpty() ? null : boundaries[0], null);
        } else {
            return new NumberInterval(null, null, value);
        }
    }

    /**
     * Get string representing interval.
     * 
     * @param interval
     *            Interval to convert to string.
     * @return String representation of interval.
     */
    public static String getStringForInterval(NumberInterval interval) {
        if (interval.getEqualsValue() != null) {
            return interval.getEqualsValue();
        } else {
            String min = interval.getGreaterThanValue() == null ? "" : interval.getGreaterThanValue();
            String max = interval.getLessThanValue() == null ? "" : interval.getLessThanValue();
            return String.format("%s-%s", min, max);
        }
    }

    /**
     * Get string representing {@link DateInterval}.
     * 
     * @param interval
     *            Interval to convert to string.
     * @return String representation of interval.
     */
    public static String getStringForInterval(DateInterval interval) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String min = interval.getFrom() == null ? "" : format.format(interval.getFrom());
        String max = interval.getTo() == null ? "" : format.format(interval.getTo());
        return String.format("%s-%s", min, max);
    }

    /**
     * Get {@link DateInterval} from string value.
     * 
     * @param value
     *            Interval value represented as string.
     * @return Interval.
     */
    public static DateInterval getDateInterval(String value) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String[] boundaries = value.split("-", -1);
        try {
            return new DateInterval(boundaries[0].isEmpty() ? null : format.parse(boundaries[0]), boundaries[1].isEmpty() ? null : format.parse(boundaries[1]));
        } catch (ParseException ex) {
            return null;
        }
    }

    private String uriFragment;

    /**
     * Constructor with URI fragment.
     * 
     * @param uriFragment
     *            URI fragment representing parameters.
     */
    public ParametersHandler(String uriFragment) {
        this.uriFragment = uriFragment;
    }

    /**
     * Gets current URI fragment.
     * 
     * @return URI fragment.
     */
    public String getUriFragment() {
        return uriFragment;
    }

    /**
     * Add parameter to URI fragment.
     * 
     * @param name
     *            Name of the parameter.
     * @param value
     *            Value of the parameter.
     */
    public void addParameter(String name, String value) {
        try {
            value = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            //TODO: Invalid parameter - ignore?
            return;
        }
        String parameter = String.format("/%s=", name);
        String parameter2 = String.format("&%s=", name);
        if (uriFragment.contains(parameter) || uriFragment.contains(parameter2)) {
            int start = Math.max(uriFragment.indexOf(parameter), uriFragment.indexOf(parameter2)) + 1;
            int end = uriFragment.indexOf('&', start);
            if (end < 0) {
                end = uriFragment.length();
            }
            uriFragment = uriFragment.substring(0, start) + String.format("%s=%s", name, value) + uriFragment.substring(end);
        } else {
            if (!uriFragment.contains("/")) {
                uriFragment += '/';
            } else {
                uriFragment += '&';
            }

            uriFragment += String.format("%s=%s", name, value);
        }
    }

    /**
     * Get value of parameter.
     * 
     * @param parameterName
     *            Name of the parameter.
     * @return Value of the parameter.
     */
    public Object getValue(String parameterName) {
        //TODO: Properly implement if needed.
        return null;
    }

    /**
     * Build configuration from URI fragment with parameters.
     * 
     * @param parameters
     *            URI fragment holding parameters.
     * @return Map with parameters.
     */
    public static Map<String, String> getConfiguration(String parameters) {
        HashMap<String, String> configuration = new HashMap<>();
        if (parameters.isEmpty()) {
            return null;
        }
        String[] pars = parameters.split("&");
        for (String parWhole : pars) {
            String[] parParts = parWhole.split("=");
            if (parParts.length != 2) {
                //TODO: Invalid parameter - ignore?
                continue;
            }
            try {
                configuration.put(parParts[0], URLDecoder.decode(parParts[1], "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                //TODO: Invalid parameter - ignore?
            }
        }
        return configuration;
    }

    /**
     * Remove parameter.
     * 
     * @param name
     *            Name of the parameter to remove.
     */
    public void removeParameter(String name) {
        String parameter = String.format("/%s=", name);
        String parameter2 = String.format("&%s=", name);
        if (uriFragment.contains(parameter) || uriFragment.contains(parameter2)) {
            int start = Math.max(uriFragment.indexOf(parameter), uriFragment.indexOf(parameter2)) + 1;
            int end = uriFragment.indexOf('&', start);
            boolean isLast = false;
            if (end < 0) {
                isLast = true;
                end = uriFragment.length();
            }
            if (isLast) {
                uriFragment = uriFragment.substring(0, start - 1);
            } else {
                uriFragment = uriFragment.substring(0, start) + uriFragment.substring(end + 1);
            }
        }
    }
}
