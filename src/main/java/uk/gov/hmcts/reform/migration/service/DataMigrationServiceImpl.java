package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ObjectMapper objectMapper;

    @Autowired
    public DataMigrationServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails == null ? false : true;
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> caseData) {
        if (caseData == null) {
            return null;
        }

        setOrgPolicy(caseData, applicant1OrganisationPolicyKey, UserRole.APPLICANT_1_SOLICITOR);
        setOrgPolicy(caseData, applicant2OrganisationPolicyKey, UserRole.APPLICANT_2_SOLICITOR);

        return caseData;
    }

    private void setOrgPolicy(Map<String, Object> caseData, String orgPolicyKey, UserRole solRole) {
        OrganisationPolicy<UserRole> orgPolicy = getOrInitializeOrgPolicy(caseData, orgPolicyKey);

        if (orgPolicy.getOrganisation() != null && orgPolicy.getOrgPolicyCaseAssignedRole() == solRole) {
            return;
        }

        setDefaultOrgPolicyFields(orgPolicy, solRole);

        caseData.put(orgPolicyKey, orgPolicy);
    }

    @SuppressWarnings("unchecked")
    private OrganisationPolicy<UserRole> getOrInitializeOrgPolicy(Map<String, Object> data, String orgPolicyKey) {
        return objectMapper.convertValue(Optional.ofNullable(data.get(orgPolicyKey))
            .orElseGet(OrganisationPolicy::new), OrganisationPolicy.class);
    }

    private void setDefaultOrgPolicyFields(OrganisationPolicy<UserRole> organisationPolicy, UserRole solicitorRole) {
        organisationPolicy.setOrgPolicyCaseAssignedRole(solicitorRole);

        organisationPolicy.setOrganisation(
            Optional.ofNullable(organisationPolicy.getOrganisation())
                .orElse(new Organisation(null, null))
        );
    }
}
