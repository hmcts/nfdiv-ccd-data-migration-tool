package uk.gov.hmcts.reform.migration.query;

import lombok.Builder;

@Builder
public class ElasticSearchQuery {

    private static final String START_QUERY = """
        {
          "query": {
            "bool": {
              "must_not": [
                {
                  "terms": {
                    "state.keyword": [
                      "Draft",
                      "AwaitingApplicant1Response",
                      "AwaitingApplicant2Response",
                      "Applicant2Approved",
                      "AwaitingPayment",
                      "AwaitingHWFDecision",
                      "Withdrawn",
                      "Archived",
                      "Rejected",
                      "NewPaperCase",
                      "FinalOrderComplete"
                    ]
                  }
                }
              ],
              "should": [
                {
                  "bool": {
                    "must_not":
                      { "exists": {
                        "field": "data_classification.applicant1SolicitorOrganisationPolicy.value.Organisation"
                      }
                    }
                  }
                },
                {
                  "bool": {
                    "must_not":
                      { "exists": {
                        "field": "data_classification.applicant2SolicitorOrganisationPolicy.value.Organisation"
                      }
                    }
                  }
                },
                {
                  "bool": {
                    "must_not": [
                      { "match": {
                        "data.applicant1SolicitorOrganisationPolicy.OrgPolicyCaseAssignedRole": "[APPONESOLICITOR]"
                      } }
                    ]
                  }
                },
                {
                  "bool": {
                    "must_not": [
                      { "match": {
                        "data.applicant2SolicitorOrganisationPolicy.OrgPolicyCaseAssignedRole": "[APPTWOSOLICITOR]"
                      } }
                    ]
                  }
                }
              ],
              "minimum_should_match": 1
            }
          },
          "_source": [
            "reference",
            "state",
            "data.applicant1SolicitorOrganisationPolicy",
            "data.applicant2SolicitorOrganisationPolicy"
          ],
          "size": %s,
          "sort": [
            {
              "reference.keyword": "asc"
            }
          ]""";

    private static final String END_QUERY = "\n}";

    private static final String SEARCH_AFTER = "\"search_after\": [%s]";

    private String searchAfterValue;
    private int size;
    private boolean initialSearch;

    public String getQuery() {
        if (initialSearch) {
            return getInitialQuery();
        } else {
            return getSubsequentQuery();
        }
    }

    private String getInitialQuery() {
        return String.format(START_QUERY, size) + END_QUERY;
    }

    private String getSubsequentQuery() {
        return String.format(START_QUERY, size) + "," + String.format(SEARCH_AFTER, searchAfterValue) + END_QUERY;
    }
}
