package org.arquillian.algeron.configuration;

public class HomeResolver {

    /**
     * Method that changes any string starting with ~ to user.home property.
     *
     * @param path to change.
     * @return String with ~changed.
     */
    public static String resolveHomeDirectory(String path) {
        if (path.startsWith("~")) {
            return path.replace("~", System.getProperty("user.home"));
        }
        return path;
    }

}
