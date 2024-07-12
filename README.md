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

During the migration, CCD makes a call to nfdiv-case-api and you will also need to add a blank event to case-api for your migration to work [example PR](https://github.com/hmcts/nfdiv-case-api/pull/3841)

After merging the migration into master, it is run like a cron, using the flux configuration defined in the [cnp-flux-config](https://github.com/hmcts/cnp-flux-config/tree/master/apps/nfdiv/nfdiv-ccd-case-migration) repo.

## Testing a migration from local dev
You can test the migration in AAT without using flux:

1.) Run bootJar to create a Jar file.

2.) Trigger the migration using the name of your Jar and nfdiv secrets.

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
The case type would be NFD to run it for normal AAT cases, but you could also use your own custom case type for test cases that you have set up in preview, e.g. `NFD-3000`.

## Unit tests

To run all unit tests please execute following command :-

```bash
    ./gradlew test
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
