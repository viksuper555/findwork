package com.findwork.findwork.Entities;

import com.findwork.findwork.Entities.Users.UserCompany;
import com.findwork.findwork.Entities.Users.UserPerson;
import com.findwork.findwork.Enums.Category;
import com.findwork.findwork.Enums.JobLevel;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "saved_filters")
@Getter
@Setter
@NoArgsConstructor
public class SavedFilter {
    @Id
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    private UUID id;
    @Enumerated
    private JobLevel jobLevel;

    @Enumerated
    private Category jobCategory;

    @ManyToOne(cascade = {CascadeType.REFRESH})
    private UserPerson userPerson;

    public SavedFilter(UserPerson userPerson, JobLevel jobLevel, Category jobCategory) {
        this.userPerson = userPerson;
        this.jobLevel = jobLevel;
        this.jobCategory = jobCategory;
    }

    @Override
    public String toString() {
        return String.format("%s in %s", this.jobLevel, this.jobCategory);
    }
}
