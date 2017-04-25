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

package yass.screen;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassStartScreen extends YassScreen {
    private static final long serialVersionUID = -4447714100922318520L;
    private BufferedImage logo = null;
    private long eventMillis = -1;
    private int eventType = 0;
    private int LOAD_EVENT = 1;
    private int EXIT_EVENT = 2;


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "start";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "selectgame";
    }


    /**
     * Description of the Method
     */
    public void init() {
        getTheme();
        YassTheme.loadProperties();
        getTheme().loadSample("main.mp3");
        getTheme().loadSample("credits.mp3");
        getTheme().loadSample("menu_navigation.wav");
        getTheme().loadSample("songs_navigation.wav");
        getTheme().loadSample("menu_selection.wav");
        getTheme().loadSample("bell.wav");
        loadBackgroundImage("start_background.jpg");
    }


    /**
     * Description of the Method
     */
    public void show() {
        loadBackgroundImage("start_background.jpg");
        getTheme().interruptAll();
        getTheme().playSample("main.mp3", true);

        stopJukebox();

        if (isContinue()) {
            startTimer(15);
        } else {
            activatePlayer(-1);
        }
    }


    /**
     * Description of the Method
     */
    public void hide() {
        stopTimer();
    }


    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     */
    public void activatePlayer(int i) {
        super.activatePlayer(i);
        if (countActivePlayers() == 1) {
            startTimer(15);
        }
    }


    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyReleased(int key) {
        if (key == ESCAPE || key == LOAD) {
            eventType = 0;
            eventMillis = -1;
            repaint();
        }
        return true;
    }


    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyPressed(int key) {
        if (key == ESCAPE || key == LOAD) {
            setContinue(false);
            stopTimer();
            activatePlayer(-1);
            if (eventMillis < 0) {
                eventMillis = System.currentTimeMillis();
            }
            eventType = key == ESCAPE ? EXIT_EVENT : LOAD_EVENT;
            boolean event = System.currentTimeMillis() - eventMillis > 3000;
            if (event) {
                eventMillis = -1;
                if (eventType == LOAD_EVENT) {
                    loadSongs();
                } else if (eventType == EXIT_EVENT) {
                    gotoScreen("exit");
                }
            }
            repaint();
            return true;
        }

        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (key == SELECT[t]) {
                getTheme().playSample("menu_selection.wav", false);

                if (isContinue()) {
                    gotoScreen("selectgroup");
                } else {
                    //gotoScreen("selectgame");
                    gotoScreen("selectgroup");
                }
                return true;
            }
        }
        return false;
    }


    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        int w = getSize().width;
        int h = getSize().height;

        int x = getSize().width / 2;
        int y = getMargin() + 30;

        Font f = getBigFont();
        g2.setFont(f);
        g2.setStroke(getStandardStroke());
        FontMetrics m = g2.getFontMetrics();

        y = h / 2;

        if (eventMillis > 0) {
            String s = "";
            if (eventType == LOAD_EVENT) {
                s = getString("load");
            } else if (eventType == EXIT_EVENT) {
                s = getString("exit");
            }

            String sec = "" + (3900 - (System.currentTimeMillis() - eventMillis)) / 1000;
            s = s + " (" + sec + ")";

            TextLayout txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
            float sw = (float) txtLayout.getBounds().getWidth();
            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(x - sw / 2, y);
            g2.setColor(getTheme().getColor(2));
            g2.draw(txtLayout.getOutline(transform));
            return;
        }

        String s = countActivePlayers() < 1 ? getString("entergame") : getString(isContinue() ? "continuegame" : "startgame");
        if (isLoading()) {
            s = getString("loading");
        }

        TextLayout txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
        float sw = (float) txtLayout.getBounds().getWidth();
        AffineTransform transform = new AffineTransform();
        transform.setToTranslation(x - sw / 2, y);
        g2.setColor(getTheme().getColor(2));
        g2.draw(txtLayout.getOutline(transform));
    }
}

