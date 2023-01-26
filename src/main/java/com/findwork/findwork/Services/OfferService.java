package com.findwork.findwork.Services;

import com.findwork.findwork.Entities.*;
import com.findwork.findwork.Entities.Users.UserCompany;
import com.findwork.findwork.Entities.Users.UserPerson;
import com.findwork.findwork.Enums.Category;
import com.findwork.findwork.Enums.JobLevel;
import com.findwork.findwork.Repositories.JobApplicationRepository;
import com.findwork.findwork.Repositories.JobOfferRepository;
import com.findwork.findwork.Repositories.UserSavedOfferRepository;
import com.findwork.findwork.Requests.CreateJobOfferRequest;
import com.findwork.findwork.Requests.EditJobOfferRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OfferService {
    private final JobOfferRepository jobRepo;
    private final JobApplicationRepository applicationRepo;
    private final UserSavedOfferRepository savedOffersRepo;
    private final EntityManager entityManager;

    public JobOffer loadOfferById(UUID id) {
        return jobRepo.findJobOfferById(id);
    }

    public JobOffer createOffer(CreateJobOfferRequest r, UserCompany company) {
        JobOffer offer = new JobOffer(r.getTitle(), r.getDescription(), r.getRequirements(), r.getNiceToHave(), r.getBenefits(), r.getLocation(), r.getSalary(), JobLevel.valueOf(r.getJobLevel()), Category.valueOf(r.getJobCategory()), company);
        jobRepo.save(offer);
        return offer;
    }

    public List<JobOffer> getOffers(String search, String category, String level) {
        if (search == null && category == null && level == null)
            return jobRepo.findAll();

        var filters = parseFilters(search, level, category);
        Category jobCategory = filters.getJobCategory();
        JobLevel jobLevel = filters.getJobLevel();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobOffer> cq = cb.createQuery(JobOffer.class);
        Root<JobOffer> offer = cq.from(JobOffer.class);
        cq.select(offer);

        Predicate currentPredicate = null;
        Predicate newPredicate;
        if (search != null) {
            newPredicate = cb.like(cb.upper(offer.get("title")), "%" + search.toUpperCase() + "%");
            currentPredicate = newPredicate;
        }

        if (jobCategory != null) {
            newPredicate = cb.equal(offer.get("jobCategory"), jobCategory);

            if (currentPredicate != null)
                currentPredicate = cb.and(currentPredicate, newPredicate);
            else {
                currentPredicate = newPredicate;
            }
        }

        if (jobLevel != null) {
            newPredicate = cb.equal(offer.get("jobLevel"), jobLevel);

            if (currentPredicate != null)
                currentPredicate = cb.and(currentPredicate, newPredicate);
            else {
                currentPredicate = newPredicate;
            }
        }

        cq.where(currentPredicate);
        Query query = entityManager.createQuery(cq);

        return query.getResultList();
    }

    public void removeOffer(UUID id) throws Exception {
        JobOffer questionableOffer = jobRepo.findJobOfferById(id);

        if (questionableOffer == null)
            throw new Exception("The job offer you are referring to is not existent.");
        jobRepo.delete(questionableOffer);
        System.out.println(questionableOffer.getTitle());
    }

    public void editOffer(UUID id, EditJobOfferRequest r) throws Exception {
        JobOffer questionableOffer = jobRepo.findJobOfferById(id);
        if (questionableOffer == null)
            throw new Exception("The job offer you are referring to is not existent.");
        if (r.getTitle() != null)
            questionableOffer.setTitle(r.getTitle());
        if (r.getDescription() != null)
            questionableOffer.setDescription(r.getDescription());
        if (r.getRequirements() != null)
            questionableOffer.setRequirements(r.getRequirements());
        if (r.getNiceToHave() != null)
            questionableOffer.setNiceToHave(r.getNiceToHave());
        if (r.getBenefits() != null)
            questionableOffer.setBenefits(r.getBenefits());
        if (r.getLocation() != null)
            questionableOffer.setLocation(r.getLocation());
        if (r.getSalary() != null)
            questionableOffer.setSalary(r.getSalary());
        if (r.getJobLevel() != null)
            questionableOffer.setJobLevel(JobLevel.valueOf(r.getJobLevel()));
        if (r.getJobCategory() != null)
            questionableOffer.setJobCategory(Category.valueOf(r.getJobCategory()));
        jobRepo.save(questionableOffer);
    }

    public List<JobApplication> getOfferApplications(UUID id) {
        JobOffer offer = jobRepo.findJobOfferById(id);
        return applicationRepo.findAllByOffer(offer);
    }

    public void createApplication(UserPerson user, UUID offerId) throws Exception {
        JobOffer offer = jobRepo.findJobOfferById(offerId);
        if (applicationRepo.findJobApplicationByUserAndOffer(user, offer) != null)
            throw new Exception("You have already applied for this position!");

        JobApplication application = new JobApplication(user, offer);
        applicationRepo.save(application);
    }

    public void deleteApplication(UserPerson user, UUID offerId) throws Exception {
        JobOffer offer = jobRepo.findJobOfferById(offerId);
        if (applicationRepo.findJobApplicationByUserAndOffer(user, offer) == null)
            throw new Exception("No application found!");

        JobApplication toDelete = applicationRepo.findJobApplicationByUserAndOffer(user, offer);
        applicationRepo.delete(toDelete);
    }

    public boolean checkApplied(UserPerson user, UUID offerId) {
        JobOffer offer = jobRepo.findJobOfferById(offerId);
        JobApplication applied = applicationRepo.findJobApplicationByUserAndOffer(user, offer);

        return applied != null;
    }

    public void saveOffer(UserPerson user, UUID offerId) throws Exception {
        JobOffer offer = jobRepo.findJobOfferById(offerId);
        if (savedOffersRepo.findUserSavedOfferByUserAndOffer(user, offer) != null)
            throw new Exception("Offer already saved!");

        UserSavedOffer newSave = new UserSavedOffer(user, offer);
        savedOffersRepo.save(newSave);
    }

    public void unsaveOffer(UserPerson user, UUID offerId) throws Exception {
        JobOffer offer = jobRepo.findJobOfferById(offerId);
        if (savedOffersRepo.findUserSavedOfferByUserAndOffer(user, offer) == null)
            throw new Exception("Offer had not been saved!");

        UserSavedOffer toDelete = savedOffersRepo.findUserSavedOfferByUserAndOffer(user, offer);
        savedOffersRepo.delete(toDelete);
    }

    public boolean checkSaved(UserPerson user, UUID offerId) {
        JobOffer offer = jobRepo.findJobOfferById(offerId);
        UserSavedOffer saved = savedOffersRepo.findUserSavedOfferByUserAndOffer(user, offer);

        return saved != null;
    }
    public Filters parseFilters(String search, String level, String category){
        Category categoryEnum = null;
        JobLevel levelEnum = null;

        if (category != null && !category.equals("--Any--"))
            categoryEnum = Category.valueOf(category);
        if (level != null && !level.equals("--Any--"))
            levelEnum = JobLevel.valueOf(level);

        return new Filters(search, levelEnum, categoryEnum);
    }
}
