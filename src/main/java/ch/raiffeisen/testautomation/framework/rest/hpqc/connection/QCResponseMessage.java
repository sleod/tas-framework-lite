package ch.raiffeisen.testautomation.framework.rest.hpqc.connection;

public class QCResponseMessage {

    private final String message;
    private QCEntity qcEntity;
    private final String entityId;
    private final String entry;

    public QCResponseMessage(QCEntity qce, String message, String entry) {
        qcEntity = qce;
        this.message = message;
        this.entry = entry;
        entityId = qce.getQcEntityID();
    }

    public QCResponseMessage(String message, String entry, String entityId) {
        this.message = message;
        this.entry = entry;
        this.entityId = entityId;
        qcEntity = null;
    }

    public String getEntityId() {
        if (qcEntity == null) {
            return entityId;
        } else {
            return qcEntity.getQcEntityID();
        }
    }

    public String getMessage() {
        return message;
    }

    public String getEntry() {
        return entry;
    }
}
