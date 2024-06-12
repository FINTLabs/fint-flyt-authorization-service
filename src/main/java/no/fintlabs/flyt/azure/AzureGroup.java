package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.Group;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class AzureGroup {
    protected String id;
    protected List<String> members;

    public AzureGroup(Group group) {
        this.id = group.id;
        this.members = new ArrayList<>();
    }
}


