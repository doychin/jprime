package site.controller;

import java.util.Collections;
import java.util.List;

import jakarta.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import site.facade.AdminService;
import site.facade.BackgroundJobService;
import site.facade.BranchService;
import site.facade.TicketService;
import site.model.Branch;
import site.model.Registrant;
import site.model.Visitor;
import site.model.VisitorStatus;

/**
 * @author Ivan St. Ivanov
 */
@Controller
@RequestMapping(value = "/admin/registrant")
public class AdminRegistrantController {

    private static final Logger logger = LogManager.getLogger(AdminRegistrantController.class);

    public static final String REGISTRANT_VIEW_JSP = "admin/registrant/view";

    public static final String REGISTRANT_EDIT_JSP = "admin/registrant/edit";

    private static final String REDIRECT_ADMIN_REGISTRANT_VIEW = "redirect:/admin/registrant/view";

    @Autowired
    private AdminService adminFacade;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private BackgroundJobService jobService;

    @Autowired
    private BranchService branchService;

    @GetMapping("/view")
    public String viewRegistrants(Pageable pageable, Model model,
        @RequestParam(required = false, name = "branch") String branchForm,
        @PathVariable(required = false, name = "branch") String branchPath) {
        String branch = StringUtils.isNotBlank(branchForm) ? branchForm : branchPath;
        Branch currentBranch = branchService.getCurrentBranch();
        int selectedYear;
        Branch branchEntity;

        if (StringUtils.isNotBlank(branch)) {
            branchEntity = branchService.findById(branch);
            selectedYear = branchEntity.getYear();
        } else {
            branchEntity = currentBranch;
            selectedYear = currentBranch.getYear();
        }

        branch = branchEntity.getLabel();

        Page<Registrant> registrantsPage = adminFacade.findRegistrantsByBranch(pageable, branchEntity);
        model.addAttribute("registrants", registrantsPage.getContent());
        model.addAttribute("totalPages", registrantsPage.getTotalPages());
        model.addAttribute("number", registrantsPage.getNumber());

        model.addAttribute("branches", branchService.allBranches());
        model.addAttribute("selected_branch", branch);
        model.addAttribute("selected_year", Integer.toString(selectedYear));
        model.addAttribute("current_branch", currentBranch);

        return REGISTRANT_VIEW_JSP;
    }

    @GetMapping("/add")
    public String getNewRegistrantForm(Model model) {
        Registrant registrant = new Registrant();
        registrant.setBranch(branchService.getCurrentBranch());
        model.addAttribute("registrant", registrant);
        model.addAttribute("paymentTypes", Registrant.PaymentType.values());
        model.addAttribute("branches", branchService.allBranches());
        return REGISTRANT_EDIT_JSP;
    }

    @GetMapping("/edit/{itemId}")
    public String getEditRegistrantForm(@PathVariable Long itemId, Model model) {
        model.addAttribute("registrant", adminFacade.findOneRegistrant(itemId));
        model.addAttribute("paymentTypes", Registrant.PaymentType.values());
        model.addAttribute("branches", branchService.allBranches());
        return REGISTRANT_EDIT_JSP;
    }

    @GetMapping("/{itemId}/addVisitor")
    public String getAddVisitorToRegistrantForm(@PathVariable Long itemId, Model model) {
        Visitor visitor = new Visitor();
        visitor.setRegistrant(adminFacade.findOneRegistrant(itemId));
        model.addAttribute("visitor", visitor);
        model.addAttribute("statuses", VisitorStatus.values());
        return AdminVisitorController.VISITOR_EDIT_JSP;
    }

    @PostMapping("/add")
    public String addRegistrant(@Valid final Registrant registrant, BindingResult bindingResult,
        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("branches", branchService.allBranches());
            return REGISTRANT_EDIT_JSP;
        }
        adminFacade.saveRegistrant(registrant);
        return REDIRECT_ADMIN_REGISTRANT_VIEW;
    }

    @GetMapping(value = "/remove/{itemId}")
    public String remove(@PathVariable Long itemId) {
        adminFacade.deleteRegistrant(itemId);
        return REDIRECT_ADMIN_REGISTRANT_VIEW;
    }

    @GetMapping(value = "/send-tickets/{itemId}")
    public String sendTicketsForRegistrant(@PathVariable Long itemId) {
        Registrant registrant = adminFacade.findOneRegistrant(itemId);

        List<Visitor> visitors = registrant.getVisitors();
        if (CollectionUtils.isEmpty(visitors)) {
            return REDIRECT_ADMIN_REGISTRANT_VIEW;
        }

        if (visitors.size() > 2) {
            String jobId =
                jobService.createBackgroundJob("Sending tickets to registrant " + registrant.getName());
            jobService.runJob(jobId, visitors, this::sendTicket, this::emailErrorLog);
            return "redirect:/admin/jobs/view";
        } else {
            visitors.forEach(this::sendTicket);
        }

        return REDIRECT_ADMIN_REGISTRANT_VIEW;
    }

    private String emailErrorLog(Visitor visitor) {
        return visitor.anyEmail();
    }

    private boolean sendTicket(Visitor visitor) {
        if (StringUtils.isEmpty(visitor.anyEmail())) {
            return false;
        }

        List<Pair<Visitor, Boolean>> result =
            ticketService.generateAndSendTicketEmail(visitor.anyEmail(), Collections.singletonList(visitor));

        return result.stream().map(Pair::getValue).reduce(true, (a, b) -> a && b);
    }
}
