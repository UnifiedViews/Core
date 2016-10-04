package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

public class DpuItem implements Comparable {

    private String dpuName;
    private String jarName;
    private String version;

    public DpuItem(String dpuName, String jarName, String version) {
        this.dpuName = dpuName;
        this.jarName = jarName;
        this.version = version;
    }

    public String getDpuName() {
        return dpuName;
    }

    public void setDpuName(String dpuName) {
        this.dpuName = dpuName;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ExportedDpuItem{" +
                "dpuName='" + dpuName + '\'' +
                ", jarName='" + jarName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DpuItem that = (DpuItem) o;

        if (dpuName != null ? !dpuName.equals(that.dpuName) : that.dpuName != null) return false;
        if (jarName != null ? !jarName.equals(that.jarName) : that.jarName != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dpuName != null ? dpuName.hashCode() : 0;
        result = 31 * result + (jarName != null ? jarName.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object o) {
    	DpuItem dpuItem = (DpuItem)o;
    	int result = this.getDpuName().compareToIgnoreCase(dpuItem.getDpuName());
        return result;
    }
}
