package com.findwork.findwork.Controllers;

import com.findwork.findwork.Entities.JobApplication;
import com.findwork.findwork.Entities.JobOffer;
import com.findwork.findwork.Entities.SavedFilter;
import com.findwork.findwork.Entities.Users.UserCompany;
import com.findwork.findwork.Entities.Users.UserPerson;
import com.findwork.findwork.Enums.Category;
import com.findwork.findwork.Enums.JobLevel;
import com.findwork.findwork.Requests.CreateJobOfferRequest;
import com.findwork.findwork.Requests.EditJobOfferRequest;
import com.findwork.findwork.Services.OfferService;
import com.findwork.findwork.Services.UserService;
import com.findwork.findwork.Services.ValidationService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@AllArgsConstructor
@RequestMapping("/offers")
public class JobOfferController {
    private final ValidationService validationService;
    private final OfferService offerService;
    private final UserService userService;

    @GetMapping("/")
    public String getAllOffers(Model model, Authentication auth,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String level,
                               @RequestParam(required = false) Boolean save) {
        UserPerson user = null;
        if (auth != null) {
            user = (UserPerson) auth.getPrincipal();
        }

        List<JobOffer> offers;

        if (category != null && category.equals("--Any--"))
            category = null;

        if (level != null && level.equals("--Any--"))
            level = null;

        offers = offerService.getOffers(search, category, level);

        model.addAttribute("offers", offers);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("levels", JobLevel.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", Category.values());
        if(user != null)
        {
            List<SavedFilter> savedFilters = userService.loadSavedFiltersById(user.getId());
            model.addAttribute("savedFilters", savedFilters);
        }

        if(save != null && save){
            if(user == null){
                System.out.print("Cannot save user filters. User not logged in.");
                return "offers";
            }

            var filters = offerService.parseFilters(search, level, category);
            Category categoryEnum = filters.getJobCategory();
            JobLevel levelEnum = filters.getJobLevel();

            userService.createSavedFilter(new SavedFilter(user, levelEnum, categoryEnum));
            return "offers";
        }
        return "offers";
    }

    @GetMapping("/create")
    String getCreateOfferPage(Model model, Authentication auth) {
        UserCompany company = (UserCompany) auth.getPrincipal();
        model.addAttribute("levels", JobLevel.values());
        model.addAttribute("categories", Category.values());
        model.addAttribute("company", company);
        return "createOffer";
    }

    @PostMapping("/create")
    public String createOffer(Authentication auth, CreateJobOfferRequest request, RedirectAttributes attr) {
        UserCompany company = (UserCompany) auth.getPrincipal();
        JobOffer questionableOffer;
        try {
            validationService.validateCreateJobOfferRequest(request);
            questionableOffer = offerService.createOffer(request, company);
        } catch (Exception e) {
            attr.addFlashAttribute("error", e.getMessage());
            return "redirect:/offers/create";
        }

        return "redirect:/offers/" + questionableOffer.getId();
    }

    @PostMapping("/{id}/remove")
    public String removeOffer(@PathVariable UUID id, RedirectAttributes attr, Authentication auth) {
        UserCompany company = (UserCompany) auth.getPrincipal();
        if (!offerService.loadOfferById(id).getCompany().getId().equals(company.getId()))
            return "redirect:/offers/" + id;

        try {
            offerService.removeOffer(id);
        } catch (Exception e) {
            attr.addFlashAttribute("error", e.getMessage());
            return "redirect:/offers/" + id;
        }

        return "redirect:/company/" + company.getId();
    }

    @GetMapping("/{id}/edit")
    String getEditOfferPage(@PathVariable UUID id, Model model, Authentication auth) {
        JobOffer offer = offerService.loadOfferById(id);

        UserCompany company = (UserCompany) auth.getPrincipal();
        if (!offer.getCompany().getId().equals(company.getId()))
            return "redirect:/offers/" + id;

        model.addAttribute("offer", offer);
        model.addAttribute("levels", JobLevel.values());
        model.addAttribute("categories", Category.values());
        return "editOffer";
    }

    @GetMapping("/{id}")
    String getOfferPage(@PathVariable UUID id, Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPerson) {
            boolean saved = offerService.checkSaved((UserPerson) auth.getPrincipal(), id);
            model.addAttribute("saved", saved);

            boolean applied = offerService.checkApplied((UserPerson) auth.getPrincipal(), id);
            model.addAttribute("applied", applied);
        }

        model.addAttribute("offer", offerService.loadOfferById(id));
        return "offer";
    }

    @GetMapping("/{id}/applications")
    public String getOfferApplications(@PathVariable UUID id, Model model, Authentication auth) {
        UserCompany company = (UserCompany) auth.getPrincipal();
        if (!offerService.loadOfferById(id).getCompany().getId().equals(company.getId()))
            return "redirect:/offers/" + id;

        List<JobApplication> applications = offerService.getOfferApplications(id);

        model.addAttribute("offer", offerService.loadOfferById(id));
        model.addAttribute("applications", applications);
        return "offerApplications";
    }

    @PostMapping("/{id}")
    public String editOffer(@PathVariable UUID id, EditJobOfferRequest request, Model model, Authentication auth) {
        UserCompany company = (UserCompany) auth.getPrincipal();
        if (!offerService.loadOfferById(id).getCompany().getId().equals(company.getId()))
            return "redirect:/offers/" + id;

        try {
            validationService.validateEditJobOfferRequest(request);
            offerService.editOffer(id, request);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "editOffer";
        }

        return "redirect:/offers/" + id;
    }

    @PostMapping("/{id}/apply")
    public String apply(@PathVariable UUID id, Authentication auth, RedirectAttributes atrr) {
        UserPerson user = (UserPerson) auth.getPrincipal();
        try {
            validationService.validateUserinfo(user);
            offerService.createApplication(user, id);
        } catch (Exception e) {
            System.out.println(user.getName());
            atrr.addFlashAttribute("error", e.getMessage());
            return "redirect:/offers/" + id;
        }

        atrr.addFlashAttribute("success", "You have applied successfully!");
        return "redirect:/offers/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelApplication(@PathVariable UUID id, Authentication auth, RedirectAttributes atrr) {
        UserPerson user = (UserPerson) auth.getPrincipal();
        try {
            offerService.deleteApplication(user, id);
        } catch (Exception e) {
            atrr.addFlashAttribute("error", e.getMessage());
            return "redirect:/offers/" + id;
        }

        atrr.addFlashAttribute("success", "Canceled job application!");
        return "redirect:/offers/" + id;
    }

    @PostMapping("/{id}/save")
    public String saveOffer(@PathVariable UUID id, Authentication auth, RedirectAttributes attr) {
        UserPerson user = (UserPerson) auth.getPrincipal();
        try {
            offerService.saveOffer(user, id);
        } catch (Exception e) {
            attr.addFlashAttribute("error", e.getMessage());
            return "redirect:/offers/" + id;
        }

        attr.addFlashAttribute("success", "Offer saved!");
        return "redirect:/offers/" + id;
    }

    @PostMapping("/{id}/unsave")
    public String unsaveOffer(@PathVariable UUID id, Authentication auth, RedirectAttributes atrr) {
        UserPerson user = (UserPerson) auth.getPrincipal();
        try {
            offerService.unsaveOffer(user, id);
        } catch (Exception e) {
            atrr.addFlashAttribute("error", e.getMessage());
            return "redirect:/offers/" + id;
        }

        atrr.addFlashAttribute("success", "Offer removed from saved!");
        return "redirect:/offers/" + id;
    }
}
