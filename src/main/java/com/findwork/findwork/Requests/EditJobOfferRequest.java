package com.findwork.findwork.Requests;

import com.findwork.findwork.Entities.Users.UserCompany;
import com.findwork.findwork.Enums.Category;
import com.findwork.findwork.Enums.JobLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditJobOfferRequest {

    private String title;

    private String description;

    private String requirements;

    private String niceToHave;

    private String benefits;

    private String location;

    private String salary;

    private int viewCount;

    private JobLevel jobLevel;

    private Category jobCategory;

}
