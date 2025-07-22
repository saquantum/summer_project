package uk.ac.bristol.pojo;

import lombok.Data;

@Data
public class GroupMember {
    private Integer id;
    private Integer groupId;
    private Integer userId;
}
