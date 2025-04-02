package dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Issue {
    private String id;
    private String idReadable;
    private String summary;
    private String description;
    private String created;
    private String updated;
    private Boolean resolved;
    private Integer numberInProject;
    private Project project;
    private User reporter;
    private User updater;
    private List<CustomField> customFields;
    private List<Link> links;
    private Visibility visibility;
    private String type;
}
