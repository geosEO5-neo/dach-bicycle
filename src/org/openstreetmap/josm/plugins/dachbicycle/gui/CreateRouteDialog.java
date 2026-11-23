package org.openstreetmap.josm.plugins.dachbicycle.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.dachbicycle.model.RouteSettings;
import static org.openstreetmap.josm.tools.I18n.tr;

public class CreateRouteDialog extends JDialog {
    private final RouteSettings settings = new RouteSettings();
    private boolean okPressed = false;
    
    private final JTextField networkField, nameField, refField, operatorField;

    public CreateRouteDialog(List<Way> ways) {
        super(MainApplication.getMainFrame(), tr("Fahrradroute erstellen"), true);
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // lcn (Local Cycling Network) ist ein guter Standardwert für OSM
        networkField = new JTextField("lcn", 20); 
        nameField = new JTextField(20);
        refField = new JTextField(20);
        operatorField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel(tr("Netzwerk (lcn, rcn, ncn):")), gbc); 
        gbc.gridx = 1; add(networkField, gbc);
        
        gbc.gridy++; gbc.gridx = 0; add(new JLabel(tr("Name:")), gbc); 
        gbc.gridx = 1; add(nameField, gbc);
        
        gbc.gridy++; gbc.gridx = 0; add(new JLabel(tr("Referenz (Ref):")), gbc); 
        gbc.gridx = 1; add(refField, gbc);
        
        gbc.gridy++; gbc.gridx = 0; add(new JLabel(tr("Betreiber (Operator):")), gbc); 
        gbc.gridx = 1; add(operatorField, gbc);
        
        JButton okButton = new JButton(tr("OK"));
        okButton.addActionListener(e -> {
            settings.setNetwork(networkField.getText());
            settings.setName(nameField.getText());
            settings.setRef(refField.getText());
            settings.setOperator(operatorField.getText());
            okPressed = true;
            dispose();
        });
        
        JButton cancelButton = new JButton(tr("Abbrechen"));
        cancelButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);
        
        pack();
        setLocationRelativeTo(MainApplication.getMainFrame());
    }

    public void showDialog() {
        setVisible(true);
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    public RouteSettings getRouteSettings() {
        return settings;
    }
}