package org.simpleyaml.utils;

public class CharacterConversions {
    public static boolean canBeChar(final Object object) {
        if (object instanceof Character) {
            return true;
        }

        if (object instanceof String) {
            final String str = (String) object;

            if (str.length() == 1) {
                return true;
            }
        }

        return object instanceof Number;
    }

    public static char toChar(final Object object) {
        if (object instanceof Character) {
            return (Character) object;
        }

        if (object instanceof String) {
            final String str = (String) object;

            if (str.length() == 1) {
                return str.charAt(0);
            }
        }

        if (object instanceof Number) {
            return (char) ((Number) object).intValue();
        }

        return '\0';
    }
}
