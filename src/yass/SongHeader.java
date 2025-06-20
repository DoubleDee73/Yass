/*
 * Yass Reloaded - Karaoke Editor
 * Copyright (C) 2009-2023 Saruta
 * Copyright (C) 2023 DoubleDee
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

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class SongHeader extends JDialog implements YassSheetListener {

    private TimeSpinner gapSpinner = null;
    private TimeSpinner startSpinner = null;
    private TimeSpinner endSpinner = null;
    private TimeSpinner vgapSpinner = null;
    private JTextField mp3 = null;
    private JTextField bpmField;
    private JComboBox<String> audioSelector;
    private YassActions actions;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private List<JPanel> panels = new ArrayList<>();
    private List<JLabel> labels = new ArrayList<>();
    private List<JTextField> textFields = new ArrayList<>();
    public SongHeader(JFrame owner, YassActions actions, YassTable table) {
        super(owner);
        LOGGER.info("Init Songheader");
        if (isVisible()) {
            return;
        }

        this.actions = actions;
        YassProperties yassProperties = actions.getProperties();
        boolean darkMode = yassProperties.containsKey("dark-mode") && yassProperties
                .get("dark-mode")
                .equals("true");
        setTitle(I18.get("edit_header"));
        setResizable(false);
        setUndecorated(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
            }
        });
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        AbstractButton button;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(5));
        panels.add(panel);
        JLabel label;
        Dimension labelSize = new Dimension(120, 20);
        if (yassProperties.isShinyOrNewer()) {
            audioSelector = new JComboBox<>(new String[]{UltrastarHeaderTag.AUDIO.toString(),
                    UltrastarHeaderTag.INSTRUMENTAL.toString(), UltrastarHeaderTag.VOCALS.toString()});
            audioSelector.setMinimumSize(labelSize);
            audioSelector.setPreferredSize(labelSize);
            audioSelector.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.DESELECTED && mp3 != null) {
                    String currentFile = mp3.getText();
                    table.setAudioByTag(e.getItem().toString(), currentFile);
                }
                if (e.getStateChange() == ItemEvent.SELECTED && mp3 != null) {
                    YassRow mp3Row = table.getCommentRow(e.getItem() + ":");
                    if (mp3Row != null) {
                        mp3.setText(mp3Row.getHeaderComment());
                        actions.openMp3(table.getDir() + File.separator + mp3Row.getHeaderComment());
                    } else {
                        mp3.setText(StringUtils.EMPTY);
                    }
                }
            });
            panel.add(audioSelector);
        } else {
            label = new JLabel(I18.get("edit_audio"));
            label.setMinimumSize(labelSize);
            label.setPreferredSize(labelSize);
            panel.add(label);
            labels.add(label);
        }
        boolean debugWaveform = yassProperties.getBooleanProperty("debug-waveform");
        String audio;
        if (debugWaveform && StringUtils.isNotEmpty(table.getVocals())) {
            audio = table.getVocals();
            audioSelector.setSelectedItem(UltrastarHeaderTag.VOCALS.toString());
        } else {
            audio = StringUtils.isNotEmpty(table.getAudio()) ? table.getAudio() : table.getMP3();
        }
        mp3 = new JTextField(audio);
        mp3.setName("mp3");
        YassUtils.addChangeListener(mp3, e -> {
            String selectedAudio = audioSelector != null && audioSelector.getSelectedItem() != null ?
                    audioSelector.getSelectedItem().toString() : StringUtils.EMPTY;
            String currentText = mp3.getText();
            if (currentText != null) {
                table.setAudioByTag(selectedAudio, currentText);
            }
        });
        textFields.add(mp3);
        panel.add(mp3);
        button = createOpenFileButton(actions.selectAudioFile, "mp3");
        button.setIcon(actions.getIcon("open24Icon"));
        panel.add(button);
        box.add(panel);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(5));
        panels.add(panel);
        gapSpinner = new TimeSpinner(I18.get("mpop_gap"), (int) table.getGap(), (int) table.getGap() * 10);
        gapSpinner.setLabelSize(labelSize);
        gapSpinner.setSpinnerWidth(100);
        gapSpinner.getSpinner().setFocusable(false);
        gapSpinner.getSpinner().addChangeListener(e -> {
            actions.setGap(gapSpinner.getTime());
        });
        panel.add(gapSpinner);
        textFields.add(gapSpinner.getTextField());
        labels.addAll(gapSpinner.getLabels());
        panel.add(Box.createHorizontalStrut(5));
        panel.add(button = new JButton());
        button.setAction(actions.setGapHere);
        button.setToolTipText(button.getText());
        button.setText("");
        button.setIcon(actions.getIcon("gap24Icon"));
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(20, 20));
        panel.add(Box.createHorizontalStrut(5));
        panel.add(label = new JLabel(I18.get("edit_bpm_title")));
        labels.add(label);
        Dimension midDimension = new Dimension(80, 20);
        label.setPreferredSize(midDimension);
        label.setMinimumSize(midDimension);
        bpmField = new JTextField(String.valueOf(table.getBPM()));
        bpmField.setPreferredSize(midDimension);
        bpmField.setPreferredSize(midDimension);
        bpmField.addActionListener(e1 -> {
            String s = bpmField.getText();
            double bpm1 = table.getBPM();
            try {
                bpm1 = Double.parseDouble(s);
            } catch (Exception ex) {
                bpmField.setText(String.valueOf(bpm1));
            }
            table.setBPM(bpm1);
        });
        panel.add(bpmField);
        textFields.add(bpmField);
        panel.add(button = new JButton());
        button.setAction(actions.multiply);
        button.setToolTipText(button.getText());
        button.setText("");
        button.setIcon(actions.getIcon("fastforward24Icon"));
        button.setFocusable(false);
        panel.add(button = new JButton());
        button.setAction(actions.divide);
        button.setToolTipText(button.getText());
        button.setText("");
        button.setIcon(actions.getIcon("rewind24Icon"));
        button.setFocusable(false);
        panel.add(button = new JButton());
        button.setAction(actions.recalcBpm);
        button.setToolTipText(button.getText());
        button.setText("");
        button.setIcon(actions.getIcon("refresh24Icon"));
        button.setFocusable(false);
        panel.add(button = new JButton());
        button.setAction(actions.showOnlineHelpBeat);
        button.setToolTipText(button.getText());
        button.setText("");
        button.setIcon(actions.getIcon("help24Icon"));
        button.setFocusable(false);

        box.add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(5));
        panels.add(panel);
        int duration = actions.getMP3() != null ? (int) (actions.getMP3().getDuration() / 1000) : 10000;
        startSpinner = new TimeSpinner(I18.get("mpop_audio_start"), (int) table.getStart() * 1000, duration);
        startSpinner.setLabelSize(labelSize);
        startSpinner.setSpinnerWidth(100);
        startSpinner.getSpinner().setFocusable(false);
        startSpinner.getSpinner().addChangeListener(e -> {
            actions.setStart(startSpinner.getTime());
        });
        panel.add(startSpinner);
        labels.addAll(startSpinner.getLabels());
        textFields.add(startSpinner.getTextField());
        panel.add(Box.createHorizontalStrut(30));
        int end = table.getEnd() > 0 ? (int) table.getEnd() : 10000;

        endSpinner = new TimeSpinner(I18.get("mpop_audio_end"), Math.min(duration, end), Math.max(10000, duration));
        endSpinner.setLabelSize(midDimension);
        endSpinner.setSpinnerWidth(100);
        endSpinner.getSpinner().setFocusable(false);
        endSpinner.getSpinner().addChangeListener(e -> {
            actions.setEnd(endSpinner.getTime());
        });
        panel.add(endSpinner);
        labels.addAll(endSpinner.getLabels());
        textFields.add(endSpinner.getTextField());
        panel.add(button = new JButton());
        button.setAction(actions.setStartHere);
        button.setToolTipText(button.getText());
        button.setText("");
        button.setIcon(actions.getIcon("bookmarks24Icon"));
        button.setFocusable(false);

        panel.add(button = new JButton());
        button.setAction(actions.removeStart);
        button.setToolTipText(button.getText());
        button.setText("");
        button.setIcon(actions.getIcon("delete24Icon"));
        button.setFocusable(false);

        panel.add(button = new JButton());
        button.setAction(actions.setEndHere);
        button.setToolTipText(button.getText());
        button.setText("");
        button.setIcon(actions.getIcon("bookmarks24Icon"));
        button.setFocusable(false);

        panel.add(button = new JButton());
        button.setAction(actions.removeEnd);
        button.setToolTipText(button.getText());
        button.setText("");
        button.setIcon(actions.getIcon("delete24Icon"));
        button.setFocusable(false);
        box.add(panel);

        add("Center", box);
        pack();
        applyTheme(yassProperties.getBooleanProperty("dark-mode"));
        setVisible(true);
        refreshLocation();
        toFront();
    }

    private JButton createOpenFileButton(Action action, String fieldName) {
        JButton button = new JButton();
        action.putValue("fileField", fieldName);
        button.setAction(action);
        return button;
    }

    @Override
    public void dispose() {
        setVisible(false);
        super.dispose();
    }

    public void refreshLocation() {
        setLocation(actions.getX() + 8, actions.getY() + 125);
        repaint();
    }

    public JTextField getTextFieldByName(String name) {
        if ("mp3".equals(name)) {
            return mp3;
        }
        return null;
    }

    public TimeSpinner getGapSpinner() {
        return gapSpinner;
    }

    public TimeSpinner getStartSpinner() {
        return startSpinner;
    }

    public TimeSpinner getEndSpinner() {
        return endSpinner;
    }

    public TimeSpinner getVgapSpinner() {
        return vgapSpinner;
    }

    public JTextField getMp3() {
        return mp3;
    }

    public JTextField getBpmField() {
        return bpmField;
    }

    public String getSelectedAudio() {
        if (audioSelector == null) {
            return UltrastarHeaderTag.MP3.name();
        } else {
            return Objects.requireNonNull(audioSelector.getSelectedItem()).toString();
        }
    }

    @Override
    public void posChanged(YassSheet source, double posMs) {
        
    }

    @Override
    public void rangeChanged(YassSheet source, int minHeight, int maxHeight, int minBeat, int maxBeat) {

    }

    @Override
    public void propsChanged(YassSheet sheet) {
        // dark mode buttons
        Border emptyBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                BorderFactory.createEmptyBorder(4, 4, 4, 4));
        Border rolloverBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                BorderFactory.createEmptyBorder(4, 4, 4, 4));
        for (Component c : getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).getModel().addChangeListener(e -> {
                    ButtonModel model = (ButtonModel) e.getSource();
                    c.setBackground(model.isRollover()
                                            ? (YassSheet.BLUE)
                                            : YassSheet.HI_GRAY_2);
                    ((JButton) c).setBorder(model.isRollover() ? rolloverBorder : emptyBorder);
                });
            }
            if (c instanceof JToggleButton) {
                ((JToggleButton) c).getModel().addChangeListener(e -> {
                    ButtonModel model = (ButtonModel) e.getSource();
                    c.setBackground(model.isRollover()
                                            ? (YassSheet.BLUE)
                                            : YassSheet.HI_GRAY_2);
                    ((JToggleButton) c).setBorder(model.isRollover() ? rolloverBorder : emptyBorder);
                });
            }
        }
    }
    
    public void applyTheme(boolean darkMode) {
        audioSelector.setBackground(darkMode ? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.white);
        audioSelector.setForeground(darkMode ? YassSheet.white : null);
        for (JTextField textField : textFields) {
            textField.setBackground(darkMode ? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.white);
            textField.setForeground(darkMode ? YassSheet.white : null);
        }
        for (JPanel panel : panels) {
            panel.setBackground(darkMode ? YassSheet.HI_GRAY_2_DARK_MODE : null);
            panel.setForeground(darkMode ? YassSheet.white : null);
        }
        for (JLabel label : labels) {
            label.setBackground(darkMode ? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.white);
            label.setForeground(darkMode ? YassSheet.white : null);
        }
        repaint();
    }
}
