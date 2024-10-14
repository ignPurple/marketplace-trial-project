package net.ignpurple.marketplace.util;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class Constants {
    public static final Pattern FIELD_NAME_SEPARATOR = Pattern.compile("(?=[A-Z])");

    public static final DecimalFormat COST_FORMAT = new DecimalFormat("#,###.##");
}
