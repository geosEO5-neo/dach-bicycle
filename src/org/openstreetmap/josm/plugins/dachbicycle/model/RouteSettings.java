package org.openstreetmap.josm.plugins.dachbicycle.model;

/**
 * Speichert die Benutzereingaben für eine neue Fahrradroute.
 */
public class RouteSettings {
    private String network;
    private String name;
    private String ref;
    private String operator;

    public String getNetwork() { return network; } 
    public void setNetwork(String n) { this.network = n; }
    
    public String getName() { return name; } 
    public void setName(String n) { this.name = n; }
    
    public String getRef() { return ref; } 
    public void setRef(String r) { this.ref = r; }
    
    public String getOperator() { return operator; } 
    public void setOperator(String o) { this.operator = o; }
}