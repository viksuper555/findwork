package com.findwork.findwork.Controllers;

import com.findwork.findwork.Entities.JobOffer;
import com.findwork.findwork.Requests.EditCompanyRequest;
import com.findwork.findwork.Services.OfferService;
import com.findwork.findwork.Services.UserService;
import com.findwork.findwork.Services.ValidationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@AllArgsConstructor
@RequestMapping("/company")
public class CompanyController {

    private final UserService userService;
    private final ValidationService validationService;
    private final OfferService offerService;

    @GetMapping("/{id}/offers")
    public String getCompanyOffers(@PathVariable UUID id,  Model model) {
        List<JobOffer> offers = new ArrayList<>();
        try { offers = offerService.getCompanyOffers(id);}
        catch (Exception e)
        {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("company", userService.loadUserCompanyById(id));
        model.addAttribute("offers", offers);
        return "company";
    }

    @GetMapping("/{id}")
    public String getCompanyPage(@PathVariable UUID id, Model model)
    {
        model.addAttribute("company", userService.loadUserCompanyById(id));
        return "company";
    }

    @GetMapping("/{id}/edit")
    public String getEditPageCompany() {return "editCompany";}

    @PutMapping("/{id}")
    public String editCompany(@PathVariable UUID id, EditCompanyRequest request, Model model)
    {
        try
        {
            validationService.validateEditCompanyRequest(request);
            userService.editCompany(id, request);
        }
        catch (Exception e)
        {
            model.addAttribute("error", e.getMessage());
            return "editCompany";
        }
        return "redirect:/company/" + id;
    }

}
