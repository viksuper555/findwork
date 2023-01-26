package com.findwork.findwork.Repositories;

import com.findwork.findwork.Entities.SavedFilter;
import com.findwork.findwork.Entities.Users.UserPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface SavedFilterRepository  extends JpaRepository<SavedFilter, UUID> {
    List<SavedFilter> findAllByUserPerson_Id(UUID id);
}