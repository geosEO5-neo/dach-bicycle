package org.openstreetmap.josm.plugins.dachbicycle.validator;

import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.Severity;
import static org.openstreetmap.josm.tools.I18n.tr;

public class DachBicycleValidator extends Test {

    public DachBicycleValidator() {
        super(tr("D-A-CH Bicycle Standards"), tr("Prüft Radwege auf Normkonformität (Breite, Oberfläche) nach ERA, RVS und VSS."));
    }

    @Override
    public void visit(Way way) {
        if (way == null || !way.isUsable()) return;

        if ("cycleway".equals(way.get("highway"))) {
            checkCyclewayWidth(way);
            checkSurface(way);
        }
    }

    private void checkCyclewayWidth(Way way) {
        String widthStr = way.get("width");
        if (widthStr == null) return; 

        try {
            double width = Double.parseDouble(widthStr);
            
            if (width < 2.0) {
                TestError error = TestError.builder(this, Severity.WARNING, 101)
                    .message(tr("Radweg-Breite unter ERA-Minimum: {0}m (gefordert 2.0m)", width))
                    .primitives(way) 
                    .build();
                errors.add(error);
            }
        } catch (NumberFormatException e) {
            TestError error = TestError.builder(this, Severity.ERROR, 102)
                .message(tr("Ungültiger Breiten-Wert: ''{0}''", widthStr))
                .primitives(way)
                .build();
            errors.add(error);
        }
    }

    private void checkSurface(Way way) {
        if (!way.hasKey("surface")) {
            TestError error = TestError.builder(this, Severity.OTHER, 103)
                .message(tr("Fehlende Oberflächen-Info (surface)"))
                .primitives(way)
                .build();
            errors.add(error);
        }
    }
}