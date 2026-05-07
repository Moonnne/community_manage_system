package edu.tut.cms.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrentUser {

    private Long id;
    private String uid;
    private String userName;
    private Integer userRole;
    private Integer userStatus;
}
