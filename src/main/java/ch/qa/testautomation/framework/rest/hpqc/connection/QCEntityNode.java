package ch.qa.testautomation.framework.rest.hpqc.connection;

import java.util.List;

public class QCEntityNode {

	private QCEntity entity;
	private final List<QCEntity> leafChildern;
	private final List<QCEntity> nodeChildern;
	private String status = "";

	public List<QCEntity> getNodeChildern() {
		return nodeChildern;
	}

	public List<QCEntity> getLeafChildern() {
		return leafChildern;
	}

	public QCEntityNode(QCEntity start, List<QCEntity> nodeChildern, List<QCEntity> leafChildern) {
		entity = start;
		this.leafChildern = leafChildern;
		this.nodeChildern = nodeChildern;
		status = entity.getFieldValue("status");
	}

	public QCEntity getEntity() {
		return entity;
	}

	public String getEntityAttribute(String key) {
		return entity.getFieldValue(key);
	}

	public int getEntityType() {
		return entity.getEntityType();
	}

	public void setEntity(QCEntity entity) {
		this.entity = entity;
	}

	public boolean isDirectoryNode() {
		return nodeChildern != null && leafChildern != null;
	}

	public boolean isLeafNode() {
		return leafChildern == null && nodeChildern == null;
	}

	public String status() {
		return status;
	}

	public void setStatus(String state) {
		status = state;
	}

	@Override
	public String toString() {
		String name = entity.getFieldValue("name");
//        if (name == null) {
//            String type = entity.getEntityType();
//            String typename = QCConstants.getEntityName(Integer.parseInt(type));
//            if (typename.equals(QCConstants.ENTITY_INSTANCE)) {
//                name = "Instance of Test with ID: " + entity.getFieldValue("test-id");
//            }
//        }
		return name;
	}
}