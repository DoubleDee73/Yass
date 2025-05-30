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

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassMIDI {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    Synthesizer synth = null;
    MidiChannel[] mc = null;

    private final boolean DEBUG = false;

    public static final int VOLUME_MAX = 127;
    public static final int VOLUME_MED = 80;
    public static final int VOLUME_MIN = 30;
    private int volume = VOLUME_MED;
    /**
     * Constructor for the YassMIDI object
     */
    public YassMIDI() {
        try {
            if (DEBUG) {
                // loop through all mixers, and all source and target lines within each mixer.
                Mixer.Info[] mis = AudioSystem.getMixerInfo();
                for (Mixer.Info mi : mis) {
                    Mixer mixer = AudioSystem.getMixer(mi);
                    // e.g. com.sun.media.sound.DirectAudioDevice
                    LOGGER.info("Mixer: " + mixer.getClass().getName());
                    Line.Info[] lis = mixer.getSourceLineInfo();
                    for (Line.Info li : lis) {
                        LOGGER.info("    Source line: " + li.toString());
                        showFormats(li);
                    }
                    lis = mixer.getTargetLineInfo();
                    for (Line.Info li : lis) {
                        LOGGER.info("    Target line: " + li.toString());
                        showFormats(li);
                    }
                    Control[] cs = mixer.getControls();
                    for (Control c : cs) {
                        LOGGER.info("    Control: " + c.toString());
                    }
                }
            }

            int n = 0;// default jdk soundbank.gm 1 piano 56 trumpet
            synth = MidiSystem.getSynthesizer();
            Instrument[] instr = synth.getAvailableInstruments();
            MidiDevice.Info info = synth.getDeviceInfo();
            LOGGER.info("Synthesizer found: "+info.getName() + " v" + info.getVersion() + " " + info.getVendor());

            // changed: load instrument before opening synthesizer
            //synth.loadInstrument(instr[n]);

            if (DEBUG) LOGGER.info("Open synthesizer...");
            synth.open();
            if (DEBUG) LOGGER.info("Synthesizer opened. Now load instrument...");
            synth.loadInstrument(instr[n]);

            if (DEBUG) LOGGER.info("Getting channels...");
            mc = synth.getChannels();
            if (DEBUG) LOGGER.info("Available channels: " + mc.length);

            if (DEBUG) LOGGER.info("Program channel: set instrument");
            mc[4].allNotesOff();
            mc[4].programChange(n);
            if (DEBUG) LOGGER.info("Program channel: set volume");
            mc[4].controlChange(7, volume);
            mc[4].controlChange(91, 1);
            mc[4].controlChange(93, 1);
            LOGGER.info("Soundbank ready.");

        } catch (IllegalArgumentException e) {
            /* The soft synthesizer appears to be throwing
             * non-checked exceptions through from the sampled
		     * audio system. Ignore them and only them. */
            if (e.getMessage().startsWith("No line matching")) {
                LOGGER.info(
                        "Warning: Ignoring soft synthesizer exception from the sampled audio system: "+e.getMessage());
                return;
            }
            LOGGER.log(Level.INFO, e.getMessage(), e);
        } catch (MidiUnavailableException e) {
            Throwable t = e.getCause();
            if (t instanceof IllegalArgumentException) {
                IllegalArgumentException e2 = (IllegalArgumentException) t;
                if (e2.getMessage().startsWith("No line matching")) {
                    LOGGER.info(
                            "Warning: Ignoring soft synthesizer exception from the sampled audio system: "+e2.getMessage());
                    return;
                }
                LOGGER.log(Level.INFO, e.getMessage(), e);
            }

        } catch (Exception e) {
            System.err.println("Error: Soundbank preparation failed.");
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }

    private static void showFormats(Line.Info li) {
        if (li instanceof DataLine.Info) {
            AudioFormat[] afs = ((DataLine.Info) li).getFormats();
            for (AudioFormat af : afs) {
                LOGGER.info("        " + af.toString());
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param argv Description of the Parameter
     */
    public static void main(String argv[]) {
        new YassMIDI();
    }

    /**
     * Gets the latency attribute of the YassMIDI object
     *
     * @return The latency value
     */
    public long getLatency() {
        if (synth == null) {
            return 0;
        }
        return synth.getLatency();
    }

    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     */
    public synchronized void startPlay(int n) {
        if (mc == null) {
            return;
        }
        mc[4].setMute(false);
        mc[4].noteOn(n, VOLUME_MED);
    }

    public void playNote(int n, long length) {
        if (mc == null) {
            return;
        }
        mc[4].setMute(false);
        new Play2Thread(mc[4], n, length).start();
    }

    class Play2Thread extends Thread {
        MidiChannel midiChannel;
        int note;
        long length;
        public Play2Thread(MidiChannel midiChannel, int note, long length) {
            this.midiChannel = midiChannel;
            this.note = note;
            this.length = length;
        }

        @Override
        public void run() {
            midiChannel.noteOn(note, VOLUME_MED);
            try {
                Thread.sleep(length);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            midiChannel.noteOn(note, 0);
            midiChannel.noteOff(note);
        }
    }

    /**
     * Description of the Method
     */
    public synchronized void stopPlay() {
        if (mc == null) {
            return;
        }
        mc[4].setMute(true);
    }

    /**
     * Description of the Method
     */
    public void close() {
        if (synth == null) {
            return;
        }
        synth.close();
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        if (mc == null) {
            return;
        }
        this.volume = volume;
        mc[4].controlChange(7, volume);
    }
}
