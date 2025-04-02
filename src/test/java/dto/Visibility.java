package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Visibility {
    private String id;
    private String type;
    private List<Group> permittedGroups;
    private List<User> permittedUsers;
}
