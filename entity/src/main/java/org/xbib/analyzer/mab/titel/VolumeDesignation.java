package org.xbib.analyzer.mab.titel;

import org.xbib.entities.marc.dialects.mab.MABEntity;

public class VolumeDesignation extends MABEntity {

    private final static VolumeDesignation element = new VolumeDesignation();

    public static VolumeDesignation getInstance() {
        return element;
    }

}
