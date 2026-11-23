package org.openstreetmap.josm.plugins.dachbicycle.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;

import static org.openstreetmap.josm.tools.I18n.tr;

public class RouteGapValidator extends Test {

    public RouteGapValidator() {
        super(tr("D-A-CH Route Gaps"), tr("Erkennt topologische Lücken in Fahrrad-Routen-Relationen."));
    }

    @Override
    public void visit(Relation relation) {
        if (relation == null || !relation.isUsable()) return;

        if ("route".equals(relation.get("type")) && "bicycle".equals(relation.get("route"))) {
            findGapsInRoute(relation);
        }
    }

    private void findGapsInRoute(Relation route) {
        List<Way> ways = route.getMembers().stream()
            .filter(m -> m.getMember() instanceof Way)
            .map(m -> (Way) m.getMember())
            .collect(Collectors.toList());

        if (ways.size() < 2) return; 

        for (int i = 0; i < ways.size() - 1; i++) {
            Way currentWay = ways.get(i);
            Way nextWay = ways.get(i + 1);

            if (!areWaysConnected(currentWay, nextWay)) {
                Node[] closestNodes = findClosestNodes(currentWay, nextWay);
                
                if (closestNodes[0] != null && closestNodes[1] != null && 
                    closestNodes[0].getCoor() != null && closestNodes[1].getCoor() != null) {
                    
                    double distance = closestNodes[0].getCoor().greatCircleDistance(closestNodes[1].getCoor());
                    
                    if (distance > 1.0) {
                        String routeName = route.get("name") != null ? route.get("name") : route.getUniqueId() + "";
                        String distStr = String.format("%.1f", distance);
                        
                        TestError error = TestError.builder(this, Severity.WARNING, 201)
                            .message(tr("Lücke in Fahrradroute ''{0}'': {1} m", routeName, distStr))
                            .primitives(currentWay, nextWay) 
                            .build();
                        
                        errors.add(error);
                    }
                }
            }
        }
    }

    private boolean areWaysConnected(Way way1, Way way2) {
        Set<Node> nodes1 = new HashSet<>(way1.getNodes());
        return way2.getNodes().stream().anyMatch(nodes1::contains);
    }

    private Node[] findClosestNodes(Way way1, Way way2) {
        Node closestNode1 = null, closestNode2 = null;
        double minDistance = Double.MAX_VALUE;
        for (Node node1 : way1.getNodes()) {
            for (Node node2 : way2.getNodes()) {
                if (node1.getCoor() == null || node2.getCoor() == null) continue;
                double distance = node1.getCoor().greatCircleDistance(node2.getCoor());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestNode1 = node1;
                    closestNode2 = node2;
                }
            }
        }
        return new Node[]{closestNode1, closestNode2};
    }
}