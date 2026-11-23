package org.openstreetmap.josm.plugins.dachbicycle.model;

import java.util.List;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

public class BicycleRouteManager {
    
    public Relation createRoute(List<Way> ways, RouteSettings settings) {
        Relation route = new Relation();
        
        // Standard-Tags für Fahrradrouten
        route.put("type", "route");
        route.put("route", "bicycle");
        
        // Optionale User-Tags hinzufügen
        if (settings.getNetwork() != null && !settings.getNetwork().isEmpty()) 
            route.put("network", settings.getNetwork());
            
        if (settings.getName() != null && !settings.getName().isEmpty()) 
            route.put("name", settings.getName());
            
        if (settings.getRef() != null && !settings.getRef().isEmpty()) 
            route.put("ref", settings.getRef());
            
        if (settings.getOperator() != null && !settings.getOperator().isEmpty()) 
            route.put("operator", settings.getOperator());
            
        // Alle ausgewählten Wege als Mitglieder ohne spezielle Rolle hinzufügen
        for (Way way : ways) {
            route.addMember(new RelationMember("", way));
        }
        
        return route;
    }
}