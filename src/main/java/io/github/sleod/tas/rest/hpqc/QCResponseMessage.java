package io.github.sleod.tas.rest.hpqc;

import jakarta.ws.rs.core.Response;

import java.text.MessageFormat;
import java.util.List;

public class QCResponseMessage {

    private final String message;
    private final List<QCEntity> qcEntities;
    private final String entry;
    private final int statusCode;

    public QCResponseMessage(Response response, String msg) {
        statusCode = response.getStatus();
        message = MessageFormat.format("{0} -> {1}", msg, QCConstants.getReturnStatus(statusCode));
        entry = response.readEntity(String.class);
        qcEntities = QCEntities.getQCEntitiesFromXMLString(entry);
    }

    public String getEntityId() {
        if (qcEntities.size() > 1) {
            return "0";
        } else if (qcEntities.isEmpty()) {
            return "";
        } else {
            return qcEntities.get(0).getEntityID();
        }
    }

    public String getMessage() {
        return message;
    }

    public String getEntry() {
        return entry;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getEntitiesCount() {
        return qcEntities.size();
    }

    public List<QCEntity> getQcEntities() {
        return qcEntities;
    }
}
