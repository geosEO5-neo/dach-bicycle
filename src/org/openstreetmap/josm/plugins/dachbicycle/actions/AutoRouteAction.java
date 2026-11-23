package org.openstreetmap.josm.plugins.dachbicycle.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.dachbicycle.gui.CreateRouteDialog;
import org.openstreetmap.josm.plugins.dachbicycle.model.BicycleRouteManager;
import org.openstreetmap.josm.plugins.dachbicycle.model.BicycleRouter;
import static org.openstreetmap.josm.tools.I18n.tr;

public class AutoRouteAction extends JosmAction {

    public AutoRouteAction() {
        super(tr("Auto-Routing dazwischen..."), "routing", tr("Findet automatisch den besten Weg zwischen 2 markierten Punkten"), null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        if (ds == null) return;

        // Hole die markierten Knoten (Nodes)
        List<Node> selectedNodes = ds.getSelected().stream()
            .filter(p -> p instanceof Node)
            .map(p -> (Node) p)
            .collect(Collectors.toList());

        if (selectedNodes.size() != 2) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), 
                tr("Bitte wähle exakt 2 Punkte (Nodes) auf der Karte aus (Start und Ziel)."));
            return;
        }

        Node start = selectedNodes.get(0);
        Node target = selectedNodes.get(1);

        // Suche den Pfad!
        BicycleRouter router = new BicycleRouter();
        List<Way> foundPath = router.findRoute(ds, start, target);

        if (foundPath.isEmpty()) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), 
                tr("Kein Weg gefunden! Stelle sicher, dass die Straßen verbunden sind und du alle Daten im Bereich heruntergeladen hast."));
            return;
        }

        // Markiere die gefundenen Wege in JOSM, damit der Nutzer sieht, was gewählt wurde
        ds.setSelected(foundPath);

        // Zeige unseren gewohnten Erstellungs-Dialog!
        CreateRouteDialog dialog = new CreateRouteDialog(foundPath);
        dialog.showDialog();

        if (dialog.isOkPressed()) {
            BicycleRouteManager manager = new BicycleRouteManager();
            Relation route = manager.createRoute(foundPath, dialog.getRouteSettings());
            ds.addPrimitive(route);
            ds.setSelected(route); // Markiere die fertige Relation
        }
    }
}