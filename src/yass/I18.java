/*
 * Yass - Karaoke Editor
 * Copyright (C) 2009 Saruta
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yass;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class I18 {

    /**
     * Description of the Field
     */
    public static ResourceBundle bundle = null;
    private static String lang = "en";

    private static String userdir = System.getProperty("user.home") + File.separator + ".yass" + File.separator + "i18";
    
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public static String get(String key) {
        return bundle.getString(key);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public static String getLanguage() {
        return lang;
    }

    /**
     * Sets the language attribute of the YassMain class
     *
     * @param s The new language value
     */
    public static void setLanguage(String s) {
        if (s == null) {
            s = "en";
        }
        lang = s;
        Locale loc = new Locale(lang);
        if (new File(userdir).exists()) {
            try {
                bundle = ResourceBundle.getBundle("yass", loc,
                        java.net.URLClassLoader.newInstance(new URL[]{new File(userdir).toURI().toURL()}));
            } catch (Exception e) {
                LOGGER.fine("Cannot set language " + lang);
                setDefaultLanguage();
            }
        } else {
            try {
                bundle = ResourceBundle.getBundle("yass.resources.i18.yass", loc);
            } catch (Exception e) {
                LOGGER.fine("Cannot set language " + lang);
                setDefaultLanguage();
            }
        }
    }
    public static void setDefaultLanguage() {
        lang = "en";
        Locale loc = new Locale(lang);
        try {
            bundle = ResourceBundle.getBundle("yass.resources.i18.yass", loc);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }

    /**
     * Gets the languageFolder attribute of the I18 class
     *
     * @param s Description of the Parameter
     * @return The languageFolder value
     */
    public static URL getResource(String s) {
        if (new File(userdir).exists()) {
            File f = new File(userdir + File.separator + lang + File.separator
                    + s);
            try {
                return f.toURI().toURL();
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        String filename = "/yass/resources/i18/" + lang + "/" + s;
        URL url = I18.class.getResource(filename);
        java.net.URLConnection uc = null;
        try {
            uc = url.openConnection();
            if (uc.getContentLength() > 0) {
                // LOGGER.info("i18 " + filename);
                return url;
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            try {
                if (uc != null) {
                    uc.getOutputStream().close();
                    uc.getInputStream().close();
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        filename = "/yass/resources/i18/default/" + s;
        // LOGGER.info("i18 " + filename);
        return I18.class.getResource(filename);
    }

    public static InputStream getResourceAsStream(String s) {
        if (new File(userdir).exists()) {
            File f = new File(userdir + File.separator + lang + File.separator
                    + s);
            try {
                return new FileInputStream(f);
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        String filename = "/yass/resources/i18/" + lang + "/" + s;
        InputStream is = I18.class.getResourceAsStream(filename);
        if (is != null) return is;

        filename = "/yass/resources/i18/default/" + s;
        // LOGGER.info("i18 " + filename);
        is = I18.class.getResourceAsStream(filename);
        if (is != null) return is;

        LOGGER.info("not found: " + s);
        return null;
    }

    public static Image getImage(String s) {
        if (new File(userdir).exists()) {
            File f = new File(userdir + File.separator + lang + File.separator + s);
            try {
                //LOGGER.info("reading " + f.getAbsolutePath());
                return ImageIO.read(f);
            } catch (Exception e) {}
        }

        String filename = "/yass/resources/i18/" + lang + "/" + s;
        InputStream is = I18.class.getResourceAsStream(filename);
        if (is != null) {
            try {
                //LOGGER.info("reading " + filename);
                return ImageIO.read(is);
            } catch (Exception e) {
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        filename = "/yass/resources/i18/default/" + s;
        is = I18.class.getResourceAsStream(filename);
        if (is != null) {
            try {
                //LOGGER.info("reading " + filename);
                return ImageIO.read(is);
            } catch (Exception e) {
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        LOGGER.info("not found: " + s);
        return null;
    }

    /**
     * Gets the copyright attribute of the YassActions object
     *
     * @return The copyright value
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static String getCopyright(String version, String date) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><font style=\"font-family: Arial; font-size:2em\"><u>Yass</u> Reloaded</font> by DoubleDee<br>Version: " + version + " (" + date + ")<br>");
        sb.append("<a href=\"https://github.com/DoubleDee73/Yass\">https://github.com/DoubleDee73/Yass</a><br>");
        sb.append("<br>");
        sb.append("Copyright ©2025 DoubleDee<br>");
        sb.append("<br>");
        sb.append("Forked from <a href=\"https://yass-along.com/\">Yass 2.4.3</a> by Saruta<br>");
        sb.append("This program is free software: you can redistribute it and/or modify<br>");
        sb.append("it under the terms of the GNU General Public License as published by<br>");
        sb.append("the Free Software Foundation, either version 3 of the License, or<br>");
        sb.append("(at your option) any later version.<br>");
        sb.append("<br>");
        sb.append("This program is distributed in the hope that it will be useful,<br>");
        sb.append("but WITHOUT ANY WARRANTY; without even the implied warranty of<br>");
        sb.append("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>");
        sb.append("GNU General Public License for more details.<br>");
        sb.append("<br>");
        sb.append("You should have received a copy of the GNU General Public License<br>");
        sb.append("along with this program. If not, see <a href=\"https://www.gnu.org/licenses/\">https://www.gnu.org/licenses/</a>.<br>");
        sb.append("<br>");
        sb.append("Yass uses: ");
        sb.append("Java Look & Feel Graphics Repository, JavaFX<br>");
        sb.append("iText, Jazzy Spell Checker, TeX Hyphenator, JInput, JAudioTagger, FFmpeg, FFmpeg CLI Wrapper, <br>");
        sb.append("Java Media Framework (JMF), Robert Eckstein's Wizard code,<br>");
        sb.append("juniversalchardet, Optimaize Language Detector<br>");
        sb.append("Speed measure 'Inverse Duration' based on Marcel Taeumel's approach (http://uman.sf.net).<br>");
        sb.append("Spanish translation by Pantera.<br>");
        sb.append("Hungarian translation by Skyli.<br>");
        sb.append("Licenses are stated in the help section.");
        return sb.toString();
    }
}
