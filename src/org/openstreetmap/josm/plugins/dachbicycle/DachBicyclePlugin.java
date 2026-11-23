package org.openstreetmap.josm.plugins.dachbicycle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.data.validation.OsmValidator;

// Validatoren
import org.openstreetmap.josm.plugins.dachbicycle.validator.DachBicycleValidator;
import org.openstreetmap.josm.plugins.dachbicycle.validator.RouteSuitabilityValidator;

// Actions
import org.openstreetmap.josm.plugins.dachbicycle.actions.CreateRouteAction;
import org.openstreetmap.josm.plugins.dachbicycle.actions.AutoRouteAction;

public class DachBicyclePlugin extends Plugin {

    // Variable, die sich merkt, ob das Menü schon gezeichnet wurde
    private JMenu bicycleMenu = null; 

    public DachBicyclePlugin(PluginInformation info) {
        super(info);
        
        OsmValidator.addTest(DachBicycleValidator.class);
        OsmValidator.addTest(RouteSuitabilityValidator.class);
    }
    
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null && oldFrame == null) {
            initializeMenus();
        }
    }
    
    private void initializeMenus() {
        if (MainApplication.getMenu() != null && MainApplication.getMenu().toolsMenu != null) {
            
            // DER SCHUTZSCHILD: Wenn das Menü schon da ist, brich ab!
            if (bicycleMenu != null) return;
            
            JMenu toolsMenu = MainApplication.getMenu().toolsMenu;
            bicycleMenu = new JMenu("D-A-CH Bicycle");
            
            bicycleMenu.add(new JMenuItem(new CreateRouteAction()));
            bicycleMenu.add(new JMenuItem(new AutoRouteAction()));
            
            toolsMenu.add(bicycleMenu);
        }
    }
}