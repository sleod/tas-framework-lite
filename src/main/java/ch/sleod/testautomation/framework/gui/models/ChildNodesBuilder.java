
package ch.sleod.testautomation.framework.gui.models;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

public class ChildNodesBuilder {

    public static void createChildren(File fileRoot, DefaultMutableTreeNode node) {
        File[] files = fileRoot.listFiles();
        if (files == null) {
            return;
        } else if (files.length == 0) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode("empty");
            node.add(childNode);
        }

        for (File file : files) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file), file.isDirectory());
            node.add(childNode);
            if (file.isDirectory()) {
                createChildren(file, childNode);
            }
        }
    }

    public static void addNewChild(File fileRoot, DefaultMutableTreeNode pNode) {
        pNode.add(new DefaultMutableTreeNode(new FileNode(fileRoot), fileRoot.isDirectory()));
    }

}
