package com.ecotioco.gaze.data;

import java.util.Locale;

public class AppConfig {

    // tinting category icon
    public static final boolean TINT_CATEGORY_ICON = true;

    /* Locale.US        -> 2,365.12
     * Locale.GERMANY   -> 2.365,12
     */
    public static final Locale PRICE_LOCAL_FORMAT = Locale.US;

    /* true     -> 2.365,12
     * false    -> 2.365
     */
    public static final boolean PRICE_WITH_DECIMAL = true;

    /* true     -> 2.365,12 USD
     * false    -> USD 2.365
     */
    public static final boolean PRICE_CURRENCY_IN_END = true;

}
