package site.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import site.app.Application;
import site.facade.BranchService;
import site.facade.DefaultBranchUtil;
import site.model.*;
import site.repository.ArticleRepository;
import site.repository.PartnerRepository;
import site.repository.SpeakerRepository;
import site.repository.SponsorRepository;
import site.repository.SubmissionRepository;
import site.repository.TagRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * @author Ivan St. Ivanov
 */
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@Transactional
class IndexControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private SpeakerRepository speakerRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    private Sponsor google;

    private Sponsor apple;

    private Sponsor sap;

    private Tag tag1;

    private Tag tag2;

    private Speaker brianGoetz;

    @Autowired
    private BranchService branchService;

    @BeforeAll
    public static void beforeAll(@Autowired BranchService branchService) {
        DefaultBranchUtil.createDefaultBranch(branchService);
    }

    @BeforeEach
    void setup() throws IOException {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        sponsorRepository.deleteAll();
        tagRepository.deleteAll();
        articleRepository.deleteAll();
        partnerRepository.deleteAll();
        submissionRepository.deleteAll();
        speakerRepository.deleteAll();

        google = new Sponsor(SponsorPackage.GOLD, "Google", "http://www.google.com", "sponsor@google.com");
        apple = new Sponsor(SponsorPackage.GOLD, "Apple", "http://www.apple.com", "sponsor@apple.com");
        sap = new Sponsor(SponsorPackage.PLATINUM, "SAP", "http://www.sap.com", "sponsor@sap.com");
        sap.setLogo(Files.readAllBytes(Paths.get("src/main/resources/static/images/sap.png")));
        Sponsor hater =
            new Sponsor(SponsorPackage.SILVER, "Now I hate Java", "http://hatejava.com", "hater@hatejava.com",
                false);
        sponsorRepository.save(google);
        sponsorRepository.save(apple);
        sponsorRepository.save(sap);
        sponsorRepository.save(hater);

        tag1 = tagRepository.save(new Tag("tag1"));
        tag2 = tagRepository.save(new Tag("tag2"));

        Branch currentBranch = branchService.getCurrentBranch();
        assertThat(currentBranch).isNotNull();

        brianGoetz =
            new Speaker("Brian", "Goetz", "brian@oracle.com", "The Java Language Architect", "@briangoetz");
        brianGoetz = speakerRepository.save(brianGoetz);
        Submission submission = submissionRepository.save(
            new Submission("title", "description", SessionLevel.BEGINNER, SessionType.CONFERENCE_SESSION,
                brianGoetz, SubmissionStatus.SUBMITTED, true).branch(currentBranch));
        brianGoetz.getSubmissions().add(submission);
        brianGoetz = speakerRepository.save(brianGoetz);

        Speaker ivanIvanov =
            new Speaker("Ivan St.", "Ivanov", "ivan@jprime.io", "JBoss Forge", "@ivan_stefanov");
        speakerRepository.save(ivanIvanov);
        submission = submissionRepository.save(
            new Submission("title", "description", SessionLevel.BEGINNER, SessionType.CONFERENCE_SESSION,
                ivanIvanov, SubmissionStatus.SUBMITTED, false).branch(currentBranch));
        ivanIvanov.getSubmissions().add(submission);
        speakerRepository.save(ivanIvanov);

        Partner devoxx = new Partner();
        devoxx.setCompanyName("devoxx");
        devoxx.setPartnerPackage(PartnerPackage.SUPPORTERS);
        partnerRepository.save(devoxx);

        Partner softUni = new Partner();
        softUni.setCompanyName("SoftUni");
        softUni.setPartnerPackage(PartnerPackage.MEDIA);
        partnerRepository.save(softUni);

        Partner baristo = new Partner();
        baristo.setCompanyName("Baristo");
        baristo.setPartnerPackage(PartnerPackage.OTHER);
        partnerRepository.save(baristo);
    }

    @Test
    void controllerShouldContainRequiredData() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name(IndexController.PAGE_INDEX))
            .andExpect(model().attribute("platinumSponsors", containsInAnyOrder(sap)))
            .andExpect(model().attribute("goldSponsors", containsInAnyOrder(google, apple)))
            .andExpect(model().attribute("silverSponsors", hasSize(0)))
            .andExpect(model().attribute("tags", containsInAnyOrder(tag1, tag2)))
            .andExpect(model().attribute("featuredSpeakers", contains(brianGoetz)))
            .andExpect(
                model().attribute("officialSupporterPartnersChunks", IsInstanceOf.instanceOf(List.class)))
            .andExpect(model().attribute("mediaPartnersChunks", IsInstanceOf.instanceOf(List.class)))
            .andExpect(model().attribute("eventPartnerChunks", IsInstanceOf.instanceOf(List.class)));
    }
}
