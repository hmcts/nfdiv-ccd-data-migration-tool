# nfdiv-ccd-case-migration

This is a No-Fault Divorce clone of the CCD Case Migration Starter, which provides a framework for data migrations within CCD. The framework runs the following process:

![diagram](docs/process.png)


CCD Case Migration Starter framework source code is located in HMCTS GitHub repository  https://github.com/hmcts/ccd-case-migration-starter


## Getting started


1. Clone the GitHub repository and create a branch for the migration task.

2. Make the required source code changes for the migration task (see section below) and create a pull request.

4. Merge into master.

5. Parameterise the migration job in the cnp-flux-config repo (including job timing, batch size, case reference etc).

## Code changes for a new migration

To set up a new migration, define an elastic search query to retrieve the cases that should be migrated in [ElasticSearchQuery.java](https://github.com/hmcts/nfdiv-ccd-case-migration/blob/master/src/main/java/uk/gov/hmcts/reform/migration/query/ElasticSearchQuery.java) and add the business logic for the migration in [DataMigrationServiceImpl.java](https://github.com/hmcts/nfdiv-ccd-case-migration/blob/master/src/main/java/uk/gov/hmcts/reform/migration/service/DataMigrationServiceImpl.java).

The migrations are recorded in case history by a new case event. The name and description of this event can be configured in [CaseMigrationProcessor.java](https://github.com/hmcts/nfdiv-ccd-case-migration/blob/master/src/main/java/uk/gov/hmcts/reform/migration/CaseMigrationProcessor.java).

During the migration, CCD makes a call to nfdiv-case-api and you will also need to add a blank event to the api for your migration to work ([example PR](https://github.com/hmcts/nfdiv-case-api/pull/3841)).

After merging the migration into master, it is run like a cron, using the flux configuration defined in the [cnp-flux-config](https://github.com/hmcts/cnp-flux-config/tree/master/apps/nfdiv/nfdiv-ccd-case-migration) repo.

## Testing the Elastic Search query in local dev
If you have set up a project locally that includes CCD (e.g. nfdiv-case-api), you should find that entries in your local DB are automatically indexed by elastic search and made available through a Docker container. By sending a get request to the container, you can test the ES query:

Example request structure:
```bash
curl -X GET "localhost:9200/nfd_cases-000001/_search?pretty" -H 'Content-Type: application/json' -d 'query'
```

Example request with a query included:
```bash
curl -X GET "localhost:9200/nfd_cases-000001/_search?pretty" -H 'Content-Type: application/json' -d '{
  "query": {
    "bool": {
      "must_not": [
        {
          "terms": {
            "data.state": ["Draft"]
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
        }
      ]
    }
  },
  "_source": [
    "reference",
    "state"
  ],
  "size": 100,
  "sort": [
    {
      "reference.keyword": "asc"
    }
  ]
}'
```

## Testing the entire migration job in local dev
You can also test the full migration job in AAT from your local development environment:

1.) Run bootJar to create a Jar file.

2.) Trigger the migration using the name of the Jar and nfdiv secrets from KeyVaults.

```bash
java -jar \
    -Dspring.application.name="nfdiv-ccd-case-migration" \
    -Didam.api.url="https://idam-api.aat.platform.hmcts.net" \
    -Didam.client.id="[SECRET_FROM_VAULT]" \
    -Didam.client.secret="[SECRET_FROM_VAULT]" \
    -Didam.client.redirect_uri="https://nfdiv.aat.platform.hmcts.net/oauth2/callback" \
    -Dcore_case_data.api.url="http://ccd-data-store-api-aat.service.core-compute-aat.internal" \
    -Didam.s2s-auth.url="http://rpe-service-auth-provider-aat.service.core-compute-aat.internal" \
    -Didam.s2s-auth.microservice="nfdiv_case_api" \
    -Didam.s2s-auth.totp_secret="[SECRET_FROM_VAULT]" \
    -Dmigration.idam.username="[SECRET_FROM_VAULT]" \
    -Dmigration.idam.password="[SECRET_FROM_VAULT]" \
    -Dmigration.jurisdiction="DIVORCE" \
    -Dmigration.caseType="NFD" \
    -Dcase-migration.elasticsearch.querySize="100" \
    -Dcase-migration.enabled=true \
    -Dlogging.level.root="ERROR" \
    -Dlogging.level.uk.gov.hmcts.reform="INFO" \
    -Dfeign.client.config.default.connectTimeout="60000" \
    -Dfeign.client.config.default.readTimeout="60000" \
    PATH/TO_MIGRATION.jar
```
The case type would be NFD to migrate standard AAT cases. However, you could choose to use your own custom casetype for testing (e.g. `NFD-3000`), as this removes the risk of running the migration for other peoples test cases by mistake.

## Unit tests

To run all unit tests please execute following command :-

```bash
    ./gradlew test
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
