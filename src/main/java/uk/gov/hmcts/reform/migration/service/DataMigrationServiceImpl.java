package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.model.UserRole;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    private final String applicant1OrganisationPolicyKey = "applicant1SolicitorOrganisationPolicy";
    private final String applicant2OrganisationPolicyKey = "applicant2SolicitorOrganisationPolicy";

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails == null ? false : true;
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> caseData) {
        if (caseData == null) {
            return null;
        }

        OrganisationPolicy<UserRole> app1OrgPolicy = initializeOrgPolicy(caseData, applicant1OrganisationPolicyKey);
        OrganisationPolicy<UserRole> app2OrgPolicy = initializeOrgPolicy(caseData, applicant2OrganisationPolicyKey);

        setDefaultOrgPolicyFields(app1OrgPolicy, UserRole.APPLICANT_1_SOLICITOR);
        setDefaultOrgPolicyFields(app2OrgPolicy, UserRole.APPLICANT_2_SOLICITOR);

        caseData.put(applicant1OrganisationPolicyKey, app1OrgPolicy);
        caseData.put(applicant2OrganisationPolicyKey, app2OrgPolicy);

        return caseData;
    }

    @SuppressWarnings("unchecked")
    private OrganisationPolicy<UserRole> initializeOrgPolicy(Map<String, Object> data, String orgPolicyKey) {
        return (OrganisationPolicy<UserRole>) Optional.ofNullable(data.get(orgPolicyKey))
            .orElseGet(OrganisationPolicy::new);
    }

    private void setDefaultOrgPolicyFields(OrganisationPolicy<UserRole> organisationPolicy, UserRole solicitorRole) {
        organisationPolicy.setOrgPolicyCaseAssignedRole(solicitorRole);

        organisationPolicy.setOrganisation(
            Optional.ofNullable(organisationPolicy.getOrganisation())
                .orElse(new Organisation(null, null))
        );
    }
}
