package com.nexes.wizard;

import yass.I18;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class Wizard extends WindowAdapter implements PropertyChangeListener{
    Hashtable<String, String> values = new Hashtable<>();

    // added by Saruta
    public String getValue(String s) {
        Object o = values.get(s);
        if (o == null) return "";
        return (String) o;
    }

    public void setValue(String s, String val) {
        if (val == null) val = "";
        values.put(s, val);
    }
    public Hashtable<String, String> getValues() { return values; }

    public static final int FINISH_RETURN_CODE = 0;
    public static final int CANCEL_RETURN_CODE = 1;
    public static final int ERROR_RETURN_CODE = 2;
        
    public static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";
    public static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";
    public static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand";
        
    static String BACK_TEXT;
    static String NEXT_TEXT;
    static String FINISH_TEXT;
    static String CANCEL_TEXT;

    static Icon BACK_ICON;
    static Icon NEXT_ICON;
    static Icon FINISH_ICON;
    static Icon CANCEL_ICON;    
    
    private WizardModel wizardModel;
    private WizardController wizardController;
    private JDialog wizardDialog;
        
    private JPanel cardPanel;
    private CardLayout cardLayout;            
    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;
    
    private int returnCode;

    
    
    /**
     * Default constructor. This method creates a new WizardModel object and passes it
     * into the overloaded constructor.
     */    
    public Wizard() {
        this((Frame)null);
    }
    
    /**
     * This method accepts a java.awt.Dialog object as the javax.swing.JDialog's
     * parent.
     * @param owner The java.awt.Dialog object that is the owner of this dialog.
     */    
    public Wizard(Dialog owner) {
        wizardModel = new WizardModel();
        wizardDialog = new JDialog(owner);         
        initComponents();
    }
 
    /**
     * This method accepts a java.awt.Frame object as the javax.swing.JDialog's
     * parent.
     * @param owner The java.awt.Frame object that is the owner of the javax.swing.JDialog.
     */    
    public Wizard(Frame owner) {
        wizardModel = new WizardModel();
        wizardDialog = new JDialog(owner);         
        initComponents();
    }
    
    /**
     * Returns an instance of the JDialog that this class created. This is useful in
     * the event that you want to change any of the JDialog parameters manually.
     * @return The JDialog instance that this class created.
     */    
    public JDialog getDialog() {
        return wizardDialog;
    }
    
    /**
     * Returns the owner of the generated javax.swing.JDialog.
     * @return The owner (java.awt.Frame or java.awt.Dialog) of the javax.swing.JDialog generated
     * by this class.
     */    
    public Component getOwner() {
        return wizardDialog.getOwner();
    }
    
    /**
     * Sets the title of the generated javax.swing.JDialog.
     * @param s The title of the dialog.
     */    
    public void setTitle(String s) {
        wizardDialog.setTitle(s);
    }
    
    /**
     * Returns the current title of the generated dialog.
     * @return The String-based title of the generated dialog.
     */    
    public String getTitle() {
        return wizardDialog.getTitle();
    }
    
    /**
     * Sets the modality of the generated javax.swing.JDialog.
     * @param b the modality of the dialog
     */    
    public void setModal(boolean b) {
        wizardDialog.setModal(b);
    }
    
    /**
     * Returns the modality of the dialog.
     * @return A boolean indicating whether or not the generated javax.swing.JDialog is modal.
     */    
    public boolean isModal() {
        return wizardDialog.isModal();
    }
    
    /**
     * Convienence method that displays a modal wizard dialog and blocks until the dialog
     * has completed.
     * @return Indicates how the dialog was closed. Compare this value against the RETURN_CODE
     * constants at the beginning of the class.
     */    
    public int showModalDialog() {
        
        wizardDialog.setModal(true);
        wizardDialog.pack();
        wizardDialog.setVisible(true);
        
        return returnCode;
    }
    
    /**
     * Returns the current model of the wizard dialog.
     * @return A WizardModel instance, which serves as the model for the wizard dialog.
     */    
    public WizardModel getModel() {
        return wizardModel;
    }
    
    /**
     * Add a Component as a panel for the wizard dialog by registering its
     * WizardPanelDescriptor object. Each panel is identified by a unique Object-based
     * identifier (often a String), which can be used by the setCurrentPanel()
     * method to display the panel at runtime.
     * @param id An Object-based identifier used to identify the WizardPanelDescriptor object.
     * @param panel The WizardPanelDescriptor object which contains helpful information about the panel.
     */    
    public void registerWizardPanel(Object id, WizardPanelDescriptor panel) {
        
        //  Add the incoming panel to our JPanel display that is managed by
        //  the CardLayout layout manager.
        
        cardPanel.add(panel.getPanelComponent(), id);
        
        //  Set a callback to the current wizard.
        
        panel.setWizard(this);
        
        //  Place a reference to it in the model. 
        
        wizardModel.registerPanel(id, panel);        
    }  
    
    /**
     * Displays the panel identified by the object passed in. This is the same Object-based
     * identified used when registering the panel.
     * @param id The Object-based identifier of the panel to be displayed.
     */    
    public void setCurrentPanel(Object id) {

        //  Get the hashtable reference to the panel that should
        //  be displayed. If the identifier passed in is null, then close
        //  the dialog.
        
        if (id == null)
            close(ERROR_RETURN_CODE);
        
        WizardPanelDescriptor oldPanelDescriptor = wizardModel.getCurrentPanelDescriptor();
        if (oldPanelDescriptor != null)
            oldPanelDescriptor.aboutToHidePanel();
        
        wizardModel.setCurrentPanel(id);
        wizardModel.getCurrentPanelDescriptor().aboutToDisplayPanel();
        
        //  Show the panel in the dialog.
        
        cardLayout.show(cardPanel, id.toString());
        wizardModel.getCurrentPanelDescriptor().displayingPanel();        
        
        
    }
    
    /**
     * Method used to listen for property change events from the model and update the
     * dialog's graphical components as necessary.
     * @param evt PropertyChangeEvent passed from the model to signal that one of its properties has changed value.
     */    
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (evt.getPropertyName().equals(WizardModel.CURRENT_PANEL_DESCRIPTOR_PROPERTY)) {
            wizardController.resetButtonsToPanelRules(); 
        } else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_TEXT_PROPERTY)) {            
            nextButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_TEXT_PROPERTY)) {            
            backButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_TEXT_PROPERTY)) {            
            cancelButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_ENABLED_PROPERTY)) {            
            nextButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ENABLED_PROPERTY)) {            
            backButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ENABLED_PROPERTY)) {            
            cancelButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_ICON_PROPERTY)) {            
            nextButton.setIcon((Icon)evt.getNewValue());
        } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ICON_PROPERTY)) {            
            backButton.setIcon((Icon)evt.getNewValue());
        } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ICON_PROPERTY)) {            
            cancelButton.setIcon((Icon)evt.getNewValue());
        }
        
    }
    
    /**
     * Retrieves the last return code set by the dialog.
     * @return An integer that identifies how the dialog was closed. See the *_RETURN_CODE
     * constants of this class for possible values.
     */    
    public int getReturnCode() {
        return returnCode;
    }
    
   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */  
    public boolean getBackButtonEnabled() {
        return wizardModel.getBackButtonEnabled().booleanValue();
    }

   /**
     * Mirrors the WizardModel method of the same name.
     * @param boolean newValue The new enabled status of the button.
     */ 
    public void setBackButtonEnabled(boolean newValue) {
        wizardModel.setBackButtonEnabled(new Boolean(newValue));
    }

   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */ 
    public boolean getNextFinishButtonEnabled() {
        return wizardModel.getNextFinishButtonEnabled().booleanValue();
    }

   /**
     * Mirrors the WizardModel method of the same name.
     * @param boolean newValue The new enabled status of the button.
     */ 
    public void setNextFinishButtonEnabled(boolean newValue) {
        wizardModel.setNextFinishButtonEnabled(new Boolean(newValue));
    }
 
   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */ 
    public boolean getCancelButtonEnabled() {
        return wizardModel.getCancelButtonEnabled().booleanValue();
    }

    /**
     * Mirrors the WizardModel method of the same name.
     * @param boolean newValue The new enabled status of the button.
     */ 
    public void setCancelButtonEnabled(boolean newValue) {
        wizardModel.setCancelButtonEnabled(new Boolean(newValue));
    }
    
    /**
     * Closes the dialog and sets the return code to the integer parameter.
     * @param code The return code.
     */    
    void close(int code) {
        returnCode = code;
        wizardDialog.dispose();
    }
    
    /**
     * This method initializes the components for the wizard dialog: it creates a JDialog
     * as a CardLayout panel surrounded by a small amount of space on each side, as well
     * as three buttons at the bottom.
     */
    
    private void initComponents() {

        wizardModel.addPropertyChangeListener(this);       
        wizardController = new WizardController(this);       

        wizardDialog.getContentPane().setLayout(new BorderLayout());
        wizardDialog.addWindowListener(this);
                
        //  Create the outer wizard panel, which is responsible for three buttons:
        //  Next, Back, and Cancel. It is also responsible a JPanel above them that
        //  uses a CardLayout layout manager to display multiple panels in the 
        //  same spot.
        
        JPanel buttonPanel = new JPanel();
        JSeparator separator = new JSeparator();
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       

        cardLayout = new CardLayout(); 
        cardPanel.setLayout(cardLayout);
        
        backButton = new JButton(new ImageIcon("com/nexes/wizard/backIcon.gif"));
        nextButton = new JButton();
        cancelButton = new JButton();
        
        backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
        nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
        cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);

        backButton.addActionListener(wizardController);
        nextButton.addActionListener(wizardController);
        cancelButton.addActionListener(wizardController);
        
        //  Create the buttons with a separator above them, then place them
        //  on the east side of the panel with a small amount of space between
        //  the back and the next button, and a larger amount of space between
        //  the next button and the cancel button.
        
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(separator, BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(cancelButton);
        
        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
        
        wizardDialog.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        wizardDialog.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);

    }
    
    private static Object getImage(String name) {

        URL url = null;

        try {
            Class<?> c = Class.forName("com.nexes.wizard.Wizard");
            url = c.getResource(name);
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Unable to find Wizard class");
        }
        return url;

    }

   /**
     * If the user presses the close box on the dialog's window, treat it
     * as a cancel.
     * @param WindowEvent The event passed in from AWT.
     */ 
    
    public void windowClosing(WindowEvent e) {
        returnCode = CANCEL_RETURN_CODE;
    }
    
    
    
    static {

        try { 
            
            PropertyResourceBundle resources = (PropertyResourceBundle)
                ResourceBundle.getBundle("com.nexes.wizard.wizard");
            
            BACK_TEXT = I18.get("wizard_back");
            NEXT_TEXT = I18.get("wizard_next");
            CANCEL_TEXT = I18.get("wizard_cancel");
            FINISH_TEXT = I18.get("wizard_finish");
            
            BACK_ICON = new ImageIcon((URL)getImage((String)(resources.getObject("backButtonIcon"))));
            NEXT_ICON = new ImageIcon((URL)getImage((String)(resources.getObject("nextButtonIcon"))));
            CANCEL_ICON = new ImageIcon((URL)getImage((String)(resources.getObject("cancelButtonIcon"))));
            FINISH_ICON = new ImageIcon((URL)getImage((String)(resources.getObject("finishButtonIcon"))));
        
        } catch (MissingResourceException mre) {
        	mre.printStackTrace();
        }
    }

}
