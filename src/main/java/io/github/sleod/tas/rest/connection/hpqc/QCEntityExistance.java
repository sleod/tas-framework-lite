package io.github.sleod.tas.rest.connection.hpqc;

public class QCEntityExistance {
    private final boolean isExist;
    private final int entityId;
    private final String responseEntry;

    public QCEntityExistance(boolean isExist, int entityId, String responseEntry) {
        this.isExist = isExist;
        this.entityId = entityId;
        this.responseEntry = responseEntry;
    }

    public String getResponseEntry() {
        return responseEntry;
    }

    public boolean isExist() {
        return isExist;
    }

    public int getEntityId() {
        return entityId;
    }
}
