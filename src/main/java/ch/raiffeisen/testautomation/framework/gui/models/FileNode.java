
package ch.raiffeisen.testautomation.framework.gui.models;

import java.io.File;

public class FileNode {

    private final File file;

    public FileNode(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        String name = file.getName();
        if (name.equals("")) {
            return file.getAbsolutePath();
        } else {
            return name;
        }
    }

    public File getFile() {
        return file;
    }

    public boolean isLeaf() {
        return file.isFile();
    }

    public boolean getAllowsChildren() {
        return file.isDirectory();
    }

    public String getPath() {
        return file.getPath();
    }

    public String getName() {
        return file.getName();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }
}
