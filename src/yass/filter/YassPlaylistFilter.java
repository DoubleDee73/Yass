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

package yass.filter;

import yass.YassActions;
import yass.YassPlayListModel;
import yass.YassSong;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassPlaylistFilter extends YassFilter {

    static Hashtable<String, Vector<String>> songs = null;
    static Hashtable<String, YassPlayListModel> playlists = null;


    /**
     * Gets the iD attribute of the YassAlbumFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "playlist";
    }


    /**
     * Gets the genericRules attribute of the YassAlbumFilter object
     *
     * @param data Description of the Parameter
     * @return The genericRules value
     */
    public String[] getGenericRules(Vector<YassSong> data) {
        Vector<String> pl = new Vector<>();
        if (songs == null) {
            songs = new Hashtable<>(1000);
            playlists = new Hashtable<>(10);
        } else {
            playlists.clear();
        }

        String plcacheName = getProperties().getProperty("playlist-cache");
        if (plcacheName == null) {
            return pl.toArray(new String[pl.size()]);
        }
        File plcache = new File(plcacheName);
        if (plcache.exists()) {
            try {
                BufferedReader inputStream = new BufferedReader(new FileReader(plcache));
                String l;
                while ((l = inputStream.readLine()) != null) {
                    YassPlayListModel plf = getPlayListFile(l);
                    pl.addElement(plf.getName());
                    playlists.put(plf.getName(), plf);
                }
                inputStream.close();
            } catch (Exception ignored) {
            }
        }

        return pl.toArray(new String[pl.size()]);
    }

    // copied from yassplaylist

    /**
     * Gets the playListFile attribute of the YassPlaylistFilter object
     *
     * @param txt Description of the Parameter
     * @return The playListFile value
     */
    public YassPlayListModel getPlayListFile(String txt) {
        try {
            File pFile = new File(txt);
            FileReader fr = new FileReader(pFile);

            StringWriter sw = new StringWriter();
            char[] buffer = new char[1024];
            int readCount;
            while ((readCount = fr.read(buffer)) > 0) {
                sw.write(buffer, 0, readCount);
            }
            fr.close();

            return getPlayList(sw.toString());
        } catch (Exception ignored) {
        }
        return null;
    }


    /**
     * Gets the playList attribute of the YassPlaylistFilter object
     *
     * @param txt Description of the Parameter
     * @return The playList value
     */
    public YassPlayListModel getPlayList(String txt) {
        YassPlayListModel pl = new YassPlayListModel();
        try {
            BufferedReader inputStream = new BufferedReader(new StringReader(txt));
            String l;

            while ((l = inputStream.readLine()) != null) {
                StringTokenizer ll = new StringTokenizer(l, ":");
                String artist = null;
                String title;

                if (ll.hasMoreTokens()) {
                    artist = ll.nextToken().trim();
                    if (artist.startsWith("#")) {
                        if (artist.startsWith("#Playlist")) {
                            int k = artist.indexOf('\"');
                            if (k > 0) {
                                int kk = artist.indexOf('\"', k + 1);
                                if (kk > 0) {
                                    pl.setName(artist.substring(k + 1, kk));
                                }
                            }
                        }
                        if (artist.startsWith("#Name:")) {
                            int k = artist.indexOf(':');
                            pl.setName(artist.substring(k + 1).trim());
                        }
                        continue;
                    }
                }
                if (ll.hasMoreTokens()) {
                    title = ll.nextToken().trim();

                    String key = artist + " : " + title;
                    Vector<String> v = songs.get(key);
                    if (v == null) {
                        v = new Vector<>(3);
                        songs.put(key, v);
                    }
                    v.addElement(pl.getName());
                    pl.addElement(key);
                }
            }
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return pl;
    }


    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean allowDrop(String rule) {
        return false;
    }


    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean allowCoverDrop(String rule) {
        if (rule.equals("all")) {
            return false;
        }
        if (rule.equals("unspecified")) {
            return false;
        }
        return true;
    }


    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @param s    Description of the Parameter
     */
    public void drop(String rule, YassSong s) {
        if (false) {
            return;
        }

        // abandoned
        // false cognitive model; using songlist for displaying playlist irritates user

        YassPlayListModel pl = playlists.get(rule);
        if (pl == null) {
            return;
        }

        if (rule.equals("unspecified")) {
            // todo remove song from all playlists
            return;
        }

        String artist = s.getArtist();
        String title = s.getTitle();
        String key = artist + " : " + title;

        if (pl.contains(key)) {
            return;
        }
        pl.addElement(key);

        // todo: append song to playlist, ask, write
        storePlayList(pl);
    }


    /**
     * Description of the Method
     *
     * @param pl Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean storePlayList(YassPlayListModel pl) {
        String listTitle = pl.getName();

        String defDir = getProperties().getProperty("playlist-directory");
        String filename = defDir + File.separator + listTitle + ".upl";

        String listFilename = pl.getFileName();
        if (listFilename != null) {
            filename = listFilename;
        }

        int n = pl.size();
        PrintWriter outputStream = null;
        FileWriter fw = null;
        try {
            outputStream = new PrintWriter(fw = new FileWriter(filename));

            outputStream.println("######################################");
            outputStream.println("#Ultrastar Deluxe Playlist Format v1.0");
            outputStream.println("#Playlist \"" + listTitle + "\" with " + n + " Songs.");
            outputStream.println("#Created with Yass " + YassActions.VERSION + ".");
            outputStream.println("######################################");
            outputStream.println("#Name: " + listTitle);
            outputStream.println("#Songs:");

            for (Enumeration<?> en = pl.elements(); en.hasMoreElements(); ) {
                String line = (String) en.nextElement();
                outputStream.println(line);
            }
            outputStream.close();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
            }
        }
        return true;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean count() {
        return false;
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        boolean hit = false;

        if (rule.equals("all")) {
            hit = true;
        } else if (rule.equals("unspecified")) {
            String artist = s.getArtist();
            String title = s.getTitle();
            String key = artist + " - " + title;
            Vector<?> v = songs.get(key);
            if (v == null) {
                hit = true;
            }
        } else {
            String artist = s.getArtist();
            String title = s.getTitle();
            String key = artist + " : " + title;
            Vector<?> v = songs.get(key);
            if (v != null) {
                for (Enumeration<?> en = v.elements(); en.hasMoreElements(); ) {
                    String pl = (String) en.nextElement();
                    if (pl.equals(rule)) {
                        hit = true;
                    }
                }
            }
        }

        return hit;
    }
}

