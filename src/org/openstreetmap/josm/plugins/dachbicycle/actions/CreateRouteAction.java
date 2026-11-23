package org.openstreetmap.josm.plugins.dachbicycle.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.dachbicycle.gui.CreateRouteDialog;
import org.openstreetmap.josm.plugins.dachbicycle.model.BicycleRouteManager;
import static org.openstreetmap.josm.tools.I18n.tr;

public class CreateRouteAction extends JosmAction {

    public CreateRouteAction() {
        // "add" ist das Icon (grünes Plus), das JOSM standardmäßig mitbringt
        super(tr("Fahrradroute erstellen"), "add", tr("Erstellt eine neue Fahrradroute aus den ausgewählten Wegen"), null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (getLayerManager().getEditDataSet() == null) return;
        DataSet ds = getLayerManager().getEditDataSet();
        
        // 1. Hole alle aktuell markierten Wege (Ways)
        List<Way> ways = ds.getSelected().stream()
            .filter(p -> p instanceof Way)
            .map(p -> (Way) p)
            .collect(Collectors.toList());
            
        if (ways.isEmpty()) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), 
                tr("Bitte wähle zuerst mindestens einen Weg auf der Karte aus, um eine Route zu erstellen."));
            return;
        }
        
        // 2. Zeige das Fenster
        CreateRouteDialog dialog = new CreateRouteDialog(ways);
        dialog.showDialog();
        
        // 3. Wenn OK geklickt wurde: Relation bauen und einfügen!
        if (dialog.isOkPressed()) {
            BicycleRouteManager manager = new BicycleRouteManager();
            Relation route = manager.createRoute(ways, dialog.getRouteSettings());
            
            ds.addPrimitive(route);
            
            // Komfort-Feature: Die neue Route wird direkt im Editor markiert
            ds.setSelected(route);
        }
    }
}