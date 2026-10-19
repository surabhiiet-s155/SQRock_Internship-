package com.jobportal.api;

import com.jobportal.api.ApiModels.ApplicationView;
import com.jobportal.api.ApiModels.Message;
import com.jobportal.application.JobApplication;
import com.jobportal.application.JobApplicationRepository;
import com.jobportal.job.Job;
import com.jobportal.job.JobRepository;
import com.jobportal.user.AppUser;
import com.jobportal.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApplicationController {
    private final JobApplicationRepository applications;
    private final JobRepository jobs;
    private final UserRepository users;

    public ApplicationController(JobApplicationRepository applications, JobRepository jobs, UserRepository users) {
        this.applications = applications;
        this.jobs = jobs;
        this.users = users;
    }

    @PostMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    Message apply(@PathVariable Long jobId, Authentication authentication) {
        AppUser candidate = currentUser(authentication);
        Job job = jobs.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found."));
        if (applications.existsByCandidateIdAndJobId(candidate.getId(), jobId)) {
            throw new IllegalArgumentException("You have already applied to this job.");
        }
        applications.save(new JobApplication(candidate, job));
        return new Message("Your application has been sent.");
    }

    @GetMapping("/dashboard/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    List<ApplicationView> candidateApplications(Authentication authentication) {
        AppUser candidate = currentUser(authentication);
        return applications.findByCandidateIdOrderByAppliedAtDesc(candidate.getId()).stream()
                .map(ApplicationController::view).toList();
    }

    private AppUser currentUser(Authentication authentication) {
        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    static ApplicationView view(JobApplication application) {
        return new ApplicationView(application.getId(), application.getJob().getTitle(),
                application.getJob().getCompany(), application.getCandidate().getName(),
                application.getCandidate().getEmail(), application.getStatus(), application.getAppliedAt());
    }
}
