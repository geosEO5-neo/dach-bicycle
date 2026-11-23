package org.openstreetmap.josm.plugins.dachbicycle.validator;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Profiler für die Eignung von Fahrradinfrastruktur (Lastenrad, Rennrad, Sicherheit, Familie).
 */
public class RouteSuitabilityValidator extends Test {

    public RouteSuitabilityValidator() {
        super(tr("D-A-CH Route Suitability"), tr("Analysiert Fahrrad-Relationen auf ihre Eignung und Sicherheit."));
    }

    @Override
    public void visit(Relation relation) {
        if (relation == null || !relation.isUsable()) return;

        if ("route".equals(relation.get("type")) && "bicycle".equals(relation.get("route"))) {
            for (RelationMember member : relation.getMembers()) {
                if (member.isWay() && member.getWay() != null) {
                    Way way = member.getWay();
                    
                    if (way.isIncomplete() || !way.isUsable()) continue;
                    
                    checkCargoBikeSuitability(relation, way);
                    checkRacingBikeSuitability(relation, way);
                    checkTramCrossings(relation, way);
                    
                    // DIE BEIDEN NEUEN CHECKS:
                    checkInclineSuitability(relation, way);
                    checkFamilySafety(relation, way);
                }
            }
        }
    }

    private void checkCargoBikeSuitability(Relation relation, Way way) {
        for (Node node : way.getNodes()) {
            if (node.hasTag("barrier", "chicane", "bollard", "cycle_barrier", "kissing_gate", "block")) {
                String widthStr = node.get("maxwidth") != null ? node.get("maxwidth") : node.get("maxwidth:physical");
                double width = -1;
                if (widthStr != null) {
                    try { width = Double.parseDouble(widthStr.replace(",", ".")); } catch (NumberFormatException ignored) {}
                }

                if (width == -1 || width < 1.3) {
                    String routeName = relation.get("name") != null ? relation.get("name") : "Neu/Lokal";
                    TestError error = TestError.builder(this, Severity.WARNING, 301)
                        .message(tr("Lastenrad-Engstelle auf Route ''{0}'': {1}", routeName, node.get("barrier")))
                        .primitives(relation, node) 
                        .build();
                    errors.add(error);
                }
            }
        }
    }

    private void checkRacingBikeSuitability(Relation relation, Way way) {
        String surface = way.get("surface");
        String smoothness = way.get("smoothness");

        boolean badSurface = surface != null && (
            surface.equals("cobblestone") || surface.equals("sett") ||
            surface.equals("gravel") || surface.equals("unpaved") ||
            surface.equals("dirt") || surface.equals("sand") ||
            surface.equals("compacted") || surface.equals("ground")
        );

        boolean badSmoothness = smoothness != null && (
            smoothness.equals("bad") || smoothness.equals("very_bad") || 
            smoothness.equals("horrible") || smoothness.equals("impassable")
        );

        if (badSurface || badSmoothness) {
            String reason = badSurface ? "Oberfläche=" + surface : "Ebenheit=" + smoothness;
            String routeName = relation.get("name") != null ? relation.get("name") : "Neu/Lokal";
            TestError error = TestError.builder(this, Severity.OTHER, 302) 
                .message(tr("Für Rennräder ungeeignet auf Route ''{0}'' ({1})", routeName, reason))
                .primitives(relation, way) 
                .build();
            errors.add(error);
        }
    }

    private void checkTramCrossings(Relation relation, Way bikeWay) {
        for (Node node : bikeWay.getNodes()) {
            for (OsmPrimitive referrer : node.getReferrers()) {
                if (referrer instanceof Way && !referrer.equals(bikeWay)) {
                    Way crossWay = (Way) referrer;
                    if ("tram".equals(crossWay.get("railway"))) {
                        double angle = calculateIntersectionAngle(bikeWay, crossWay, node);
                        if (angle >= 0 && angle < 40.0) {
                            String routeName = relation.get("name") != null ? relation.get("name") : "Neu/Lokal";
                            TestError error = TestError.builder(this, Severity.WARNING, 303)
                                .message(tr("Sturzgefahr (Todesweiche) auf Route ''{0}'': Straßenbahn kreuzt extrem spitz (ca. {1}°)", routeName, Math.round(angle)))
                                .primitives(relation, node)
                                .build();
                            errors.add(error);
                        }
                    }
                }
            }
        }
    }

    /**
     * NEU: Prüft auf für Alltagsradler zu steile Anstiege (über 8%)
     */
    private void checkInclineSuitability(Relation relation, Way way) {
        String inclineStr = way.get("incline");
        if (inclineStr == null) return;

        boolean isSteep = false;
        if (inclineStr.equals("steep")) {
            isSteep = true;
        } else {
            try {
                // Entfernt das % Zeichen und wandelt Komma in Punkt um
                String numStr = inclineStr.replace("%", "").replace(",", ".").trim();
                double incline = Math.abs(Double.parseDouble(numStr));
                if (incline > 8.0) {
                    isSteep = true;
                }
            } catch (NumberFormatException ignored) {}
        }

        if (isSteep) {
            String routeName = relation.get("name") != null ? relation.get("name") : "Neu/Lokal";
            TestError error = TestError.builder(this, Severity.OTHER, 304) // Info/Other, da es kein echter "Fehler" ist
                .message(tr("Anstrengende Steigung auf Route ''{0}'' (incline={1})", routeName, inclineStr))
                .primitives(relation, way)
                .build();
            errors.add(error);
        }
    }

    /**
     * NEU: Prüft, ob Kinder auf Straßen > 50km/h ohne bauliche Trennung fahren müssen
     */
    private void checkFamilySafety(Relation relation, Way way) {
        String highway = way.get("highway");
        
        // Reine Rad- und Fußwege sind für Familien sicher
        if (highway == null || highway.equals("cycleway") || highway.equals("path") || highway.equals("footway") || highway.equals("pedestrian")) {
            return; 
        }

        String maxspeedStr = way.get("maxspeed");
        if (maxspeedStr == null) return; 

        int maxspeed = 0;
        try {
            maxspeed = Integer.parseInt(maxspeedStr);
        } catch (NumberFormatException e) {
            // Fängt Dinge wie "AT:urban" ab (in Österreich 50 km/h)
            if (maxspeedStr.equals("AT:urban") || maxspeedStr.equals("DE:urban")) maxspeed = 50;
            else if (maxspeedStr.equals("AT:rural") || maxspeedStr.equals("DE:rural")) maxspeed = 100;
        }

        // Wenn Autos 50 km/h oder schneller fahren...
        if (maxspeed >= 50) {
            String cycleway = way.get("cycleway");
            String cyclewayRight = way.get("cycleway:right");
            String cyclewayLeft = way.get("cycleway:left");
            String cyclewayBoth = way.get("cycleway:both");

            // ...prüfen wir, ob es einen baulich getrennten Radweg (track) gibt
            boolean hasTrack = "track".equals(cycleway) || "track".equals(cyclewayRight) || 
                               "track".equals(cyclewayLeft) || "track".equals(cyclewayBoth) || 
                               "opposite_track".equals(cycleway);

            // Wenn es KEINEN getrennten Weg gibt (sondern nur 'lane' oder gar nichts) -> Warnung!
            if (!hasTrack) {
                String routeName = relation.get("name") != null ? relation.get("name") : "Neu/Lokal";
                TestError error = TestError.builder(this, Severity.WARNING, 305)
                    .message(tr("Nicht familiengerecht auf Route ''{0}'': Mischverkehr/Mehrzweckstreifen bei Tempo {1}", routeName, maxspeedStr))
                    .primitives(relation, way)
                    .build();
                errors.add(error);
            }
        }
    }

    // Hilfsmethode zur Winkelberechnung mit echtem Java-Math
    private double calculateIntersectionAngle(Way w1, Way w2, Node intersection) {
        Node n1 = getAdjacentNode(w1, intersection);
        Node n2 = getAdjacentNode(w2, intersection);
        
        if (n1 == null || n2 == null || n1.getEastNorth() == null || n2.getEastNorth() == null || intersection.getEastNorth() == null) {
            return -1;
        }

        double dx1 = n1.getEastNorth().east() - intersection.getEastNorth().east();
        double dy1 = n1.getEastNorth().north() - intersection.getEastNorth().north();
        double angle1 = Math.toDegrees(Math.atan2(dy1, dx1));

        double dx2 = n2.getEastNorth().east() - intersection.getEastNorth().east();
        double dy2 = n2.getEastNorth().north() - intersection.getEastNorth().north();
        double angle2 = Math.toDegrees(Math.atan2(dy2, dx2));
        
        double diff = Math.abs(angle1 - angle2);
        while (diff > 90) {
            if (diff > 180) diff -= 180;
            else diff = 180 - diff;
        }
        return diff;
    }

    private Node getAdjacentNode(Way way, Node node) {
        int idx = way.getNodes().indexOf(node);
        if (idx > 0) return way.getNode(idx - 1);
        if (idx < way.getNodesCount() - 1) return way.getNode(idx + 1);
        return null;
    }
}