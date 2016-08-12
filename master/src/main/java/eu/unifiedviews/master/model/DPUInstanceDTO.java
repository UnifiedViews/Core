package eu.unifiedviews.master.model;

public class DPUInstanceDTO {
    private Long id;

    private String name;

    private String description;

    private String serializedConfiguration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSerializedConfiguration() {
        return serializedConfiguration;
    }

    public void setSerializedConfiguration(String serializedConfiguration) {
        this.serializedConfiguration = serializedConfiguration;
    }

}
