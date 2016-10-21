package eu.unifiedviews.master.model;

public class PipelineDTO {

    private Long id;

    private String name;

    private String description;

    private String userExternalId;

    private String userActorExternalId;

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

    public String getUserExternalId() {
        return userExternalId;
    }

    public void setUserExternalId(String userExternalId) {
        this.userExternalId = userExternalId;
    }

    public String getUserActorExternalId() {
        return this.userActorExternalId;
    }

    public void setUserActorExternalId(String userActorExternalId) {
        this.userActorExternalId = userActorExternalId;
    }

    @Override
    public String toString() {
        return "PipelineDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", userActorExternalId='" + this.userActorExternalId + '\'' +
                ", userExternalId=" + userExternalId +
                '}';
    }
}
