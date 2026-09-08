# Prompt used to Generate the Enriched SAD Texts:
```
Given the text below, write a natural extension that incorporates all of the provided data file names.

Requirements:
- Format the extension with exactly one sentence per line.
- Mention every data file from the provided list at least once.
- Create the content freely; it does not need to be factually based on the file names.
- Some files may be mentioned multiple times where appropriate.
- When mentioning a file multiple times, describe or reference it differently in different sentences to create natural variation.
- Integrate the file names naturally into the text rather than simply listing them.
- Also include sentences that do not mention any data files.
- Mix these non-file-related sentences naturally at various points throughout the extension rather than grouping them in one place.
- Maintain a natural and varied balance between sentences that mention data files and sentences that do not.
- Ensure the extension remains coherent and reads like a natural continuation of the original text.
- Maintain a style and tone that fits the original text.

Data files to include:
[INSERT DATA FILES HERE]

Original text:
[INSERT TEXT HERE]

Output only the extended text, with one complete sentence per line.
```
## LLM used to Generate the Enriched SAD Texts:
gpt-5.6-terra (https://ki-toolbox.scc.kit.edu/?model=azure.gpt-5.6-terra)

---

# Datafile Selection
Per evaluation project, we took 5 samples of 10 random datafiles each:
``` java
Set<String> programmingLanguageExtensions = Set.of("java","jar", "sh", "bash", "zsh", "fish", "ksh", "csh", "tcsh", "py", "pyw", "py3", "pyi", "cpp", "cc", "cxx",
                "c++", "hpp", "hh", "hxx", "h++", "inl", "asm", "s", "S", "inc", "asp", "aspx", "asa", "ashx", "asmx", "ascx", "ahk", "ahkl",
                "au3", "awk", "bat", "cmd", "c", "h", "clj", "cljs", "cljc", "edn", "cob", "cbl", "ccp", "cpy", "cobol", "coffee", "litcoffee",
                "coffee.md", "cs", "csx", "dart", "dm", "ex", "exs", "erl", "hrl", "f", "for", "f77", "f90", "f95", "f03", "f08", "f18",
                "fpp", "go", "groovy", "gvy", "gy", "gsh", "hs", "lhs", "js", "mjs", "cjs", "jsx", "jl", "kt", "kts", "lisp", "lsp", "cl",
                "l", "fasl", "lua", "m4", "m", "mat", "mlx", "mm", "ml", "mli", "mll", "mly", "pas", "pp", "p", "pl", "pm", "pod", "t",
                "psgi", "php", "php3", "php4", "php5", "php7", "php8", "phtml", "phps", "ps1", "psm1", "psd1", "ps1xml", "psc1", "pssc",
                "cdxml", "pro", "P", "pyc", "pyo", "pyx", "pxd", "pxi", "r", "R", "Rmd", "Rmarkdown", "rmd", "rb", "rbw", "rake", "gemspec",
                "ru", "rs", "scala", "sc", "smali", "sol", "swift", "tcl", "tk", "exp", "ts", "tsx", "mts", "cts", "bas", "cls", "frm", "vba",
                "v", "vh", "sql","sv", "svh", "vhd", "vhdl", "vho", "vht", "vhi", "yar", "yara", "zig","swf","pdf", "md","txt","html","htm","css","scss","snap","puml","bin",
                // Images
                "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff",
                // Video
                "mp4", "mkv", "avi", "mov", "webm", "wmv", "flv", "m4v",
                // Audio
                "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma",
                //Powerpoint
                "ppt", "pptx", "pps", "ppsx"
);
var dataFilesList = new ArrayList<>( allCodeFiles
        .stream()
        .filter(f -> !f.getExtension().isEmpty())
        .filter(f -> !programmingLanguageExtensions.contains(f.getExtension()))
        .toList());
var ambiguousDataFileNames = new ArrayList<>(dataFilesList.stream()
        .collect(Collectors.groupingBy(f -> Objects.toString(f.getName(), ""), Collectors.counting()))
        .entrySet()
        .stream()
        .filter(e -> e.getValue() > 1)
        .map(Map.Entry::getKey)
        .toList());
ambiguousDataFileNames.add("README"); //don't want the README as datafile..
dataFilesList.removeIf(f -> ambiguousDataFileNames.contains(f.getName()));
var random = new Random(42L);
for (int i = 0; i < 5; i++) {
    var shuffled = new ArrayList<>(dataFilesList);
    Collections.shuffle(shuffled, random);

    var sample = shuffled.stream().limit(10).collect(Collectors.toCollection(ArrayList::new));

    System.out.println(
            "sample " + (i) + "= " + sample.stream().map(f->f.getName()+"."+f.getExtension()).collect(Collectors.joining(", ")));
    //System.out.println(
    //        "sample " + (i) + "= " + sample.stream().map(CodeFile::toString).collect(Collectors.joining(", ")));
}
```

## Resulting Datafile Selection
Datafile names:
```
TM:
sample 0= FeedbackResponseVisibilityTest.json, feedbackSessionResultsNumscaleResults.json, teammates-pmd.xml, site.json, gradle.xml, template-questions.json, InstructorCourseEditPageE2ETest.json, ngsw-config.json, buildOutputCleanup.lock, teammates-stylelint.yml
sample 1= help-requests.yml, StudentHomePageE2ETest.json, FeedbackResponseCommentCRUDTest.json, rankOptionQuestionResponses.json, teammates-pmd.xml, build-dev.template.properties, compiler.xml, StudentCourseJoinConfirmationPageE2ETest.json, buildOutputCleanup.lock, testng-lnp.xml
sample 2= FeedbackNumScaleQuestionE2ETest.json, FeedbackRankOptionQuestionE2ETest.json, AdminSessionsPageE2ETest.json, test.ci-chrome.properties, feedbackSessionResultsNumscaleResults.json, FeedbackResponseCommentCRUDTest.json, testng-component.xml, workspace.xml, InstructorSessionIndividualExtensionPageE2ETest.json, numScaleQuestionResponses.json
sample 3= feedbackSessionResultsContribResultsRestrictedSections.json, manifest.webmanifest, FeedbackRubricQuestionE2ETest.json, AdminHomePageE2ETest.json, AutomatedSessionRemindersE2ETest.json, rankOptionQuestionResponses.json, ngsw-config.json, test.ci-firefox.properties, build.gradle, build.template.properties
sample 4= feedbackSessionResultsC1S1.json, CourseRosterDataBundle.json, Points calculation data.xlsx, checksums.lock, misc.xml, StudentNotificationsPageE2ETest.json, timezone.json, jmeter.properties, test.ci-firefox.properties, InstructorCoursesPageE2ETest.json
----------------------
ROD:
sample 0= backward.feature, relationship_indexing.feature, collection_proxy.feature, workspace.xml, .travis.yml, hash_indexing.feature, persistence.feature, append.feature, collection.feature, basic.feature
sample 1= basic.feature, .travis.yml, update.feature, relationship_indexing.feature, portability.feature, relationships.feature, hash_indexing.feature, workspace.xml, inheritence.feature, collection.feature
sample 2= append.feature, relationship_indexing.feature, update.feature, persistence.feature, relationships.feature, backward.feature, muliple_db.feature, portability.feature, .travis.yml, inheritence.feature
sample 3= relationship_indexing.feature, update.feature, append.feature, collection_proxy.feature, relationships.feature, basic.feature, muliple_db.feature, collection.feature, backward.feature, inheritence.feature
sample 4= persistence.feature, inheritence.feature, relationship_indexing.feature, collection_proxy.feature, append.feature, portability.feature, update.feature, collection.feature, .travis.yml, muliple_db.feature
----------------------
BBB:
sample 0= query_collections.yaml, doc-conversion.xml, conference.conf.xml, OpenSans-Medium.ttf, public_v_pres_page_cursor.yaml, OpenSans-Bold.ttf, te.json, az.json, messages_cs.properties, ml.json
sample 1= public_v_pres_page_writers.yaml, playback-video.nginx, OpenSans-Bold.ttf, messages_it.properties, sndfile.conf.xml, es_MX.json, Project_Default.xml, public_v_user_voice.yaml, slides.nginx, chat_0_9.xml
sample 2= messages_nl.properties, sv_SE.json, public_v_user_whiteboard.yaml, grails.tld, etherpad.service, bbb-1.1-meeting-tag, process_audio.feature, unmatched_audio_events.xml, support-bbb-recording.patch, conference.conf.xml
sample 3= logback-test.xml, log4j.properties, public_v_user_typing_private.yaml, hr.json, learning-dashboard.nginx, vi_VN.json, segm_lite_v681.tflite, bbb-webrtc-recorder.service, bbb-redis-messaging.xml, hy.json
sample 4= .env.template, webrtc-sfu.nginx, ca.json, bbb-export-annotations.service, .rubocop.yml, messages_es.properties, hr.json, cron_triggers.yaml, public_v_chat_message_public.yaml, public_v_meeting_usersPolicies.yaml
----------------------
CWA:
sample 0= app-config_broken_syntax.yaml, application-no-hour-retention.yaml, weight_negative.yaml, maven-version-rules.xml, application-enable-test-data.yaml, validation_schema.json, exposure-config.yaml, attenuation_duration.proto, cwa_reporting_public_data_region.json, test-type.json
sample 1= Submission Service.run.xml, dsc_list.proto, temporary_exposure_key_signature_list.proto, hadolint-analysis.yml, file3.txt.checksum, spring-boot-dcc_revocation.launch, file1.txt.checksum, contains_efgs_truststore.jks, rule_3.json, application-e2e.yaml
sample 2= presence_tracing_parameters.proto, distribution [spring-boot_run...].run.xml, naming_mismatch.yaml, dgc_parameters.proto, ubirchDSC.json, local_statistics.proto, application-error-batch.yaml, exposure-configuration.yaml, spring-boot-dcc_revocation.launch, ssl.p12
sample 3= daily-summaries-config.yaml, spring-boot-submission.launch, ppdd_ppa_parameters.proto, revocation_kid_list.proto, bn_rule_1.json, dependabot.yml, exposure-config_ok.yaml, risk_score_classification.proto, plausible-deniability-parameters.yaml, temporary_exposure_key_export.proto
sample 4= risk-calculation-parameters-1.15.yaml, testprivatekey.pem, validation_schema.json, dcc-validation-service-allowlist-rule.json, application-demo.yaml, submission [spring-boot_run...].run.xml, bn_rule_1.json, application-allow-list-invalid.yaml, risk_calculation_parameters.proto, application-callback-change-certificate-cn.yaml
```
Datafile paths:
```
TM:
sample 0= src/test/resources/data/FeedbackResponseVisibilityTest.json, src/web/services/test-data/feedbackSessionResultsNumscaleResults.json, static-analysis/teammates-pmd.xml, docs/site.json, .idea/gradle.xml, src/web/data/template-questions.json, src/e2e/resources/data/InstructorCourseEditPageE2ETest.json, ngsw-config.json, .gradle/buildOutputCleanup/buildOutputCleanup.lock, static-analysis/teammates-stylelint.yml
sample 1= .github/DISCUSSION_TEMPLATE/help-requests.yml, src/e2e/resources/data/StudentHomePageE2ETest.json, src/test/resources/data/FeedbackResponseCommentCRUDTest.json, src/web/app/components/question-types/question-statistics/test-data/rankOptionQuestionResponses.json, static-analysis/teammates-pmd.xml, src/main/resources/build-dev.template.properties, .idea/compiler.xml, src/e2e/resources/data/StudentCourseJoinConfirmationPageE2ETest.json, .gradle/buildOutputCleanup/buildOutputCleanup.lock, src/lnp/resources/testng-lnp.xml
sample 2= src/e2e/resources/data/FeedbackNumScaleQuestionE2ETest.json, src/e2e/resources/data/FeedbackRankOptionQuestionE2ETest.json, src/e2e/resources/data/AdminSessionsPageE2ETest.json, src/e2e/resources/test.ci-chrome.properties, src/web/services/test-data/feedbackSessionResultsNumscaleResults.json, src/test/resources/data/FeedbackResponseCommentCRUDTest.json, src/test/resources/testng-component.xml, .idea/workspace.xml, src/e2e/resources/data/InstructorSessionIndividualExtensionPageE2ETest.json, src/web/app/components/question-types/question-statistics/test-data/numScaleQuestionResponses.json
sample 3= src/web/services/test-data/feedbackSessionResultsContribResultsRestrictedSections.json, src/web/manifest.webmanifest, src/e2e/resources/data/FeedbackRubricQuestionE2ETest.json, src/e2e/resources/data/AdminHomePageE2ETest.json, src/e2e/resources/data/AutomatedSessionRemindersE2ETest.json, src/web/app/components/question-types/question-statistics/test-data/rankOptionQuestionResponses.json, ngsw-config.json, src/e2e/resources/test.ci-firefox.properties, build.gradle, src/main/resources/build.template.properties
sample 4= src/web/services/test-data/feedbackSessionResultsC1S1.json, src/test/resources/data/CourseRosterDataBundle.json, src/web/mockups/Points calculation data.xlsx, .gradle/7.5/checksums/checksums.lock, .idea/misc.xml, src/e2e/resources/data/StudentNotificationsPageE2ETest.json, src/web/data/timezone.json, src/lnp/resources/jmeter/bin/jmeter.properties, src/e2e/resources/test.ci-firefox.properties, src/e2e/resources/data/InstructorCoursesPageE2ETest.json
----------------------
ROD:
sample 0= features/backward.feature, features/relationship_indexing.feature, features/collection_proxy.feature, .idea/workspace.xml, .travis.yml, features/hash_indexing.feature, features/persistence.feature, features/append.feature, features/collection.feature, features/basic.feature
sample 1= features/basic.feature, .travis.yml, features/update.feature, features/relationship_indexing.feature, features/portability.feature, features/relationships.feature, features/hash_indexing.feature, .idea/workspace.xml, features/inheritence.feature, features/collection.feature
sample 2= features/append.feature, features/relationship_indexing.feature, features/update.feature, features/persistence.feature, features/relationships.feature, features/backward.feature, features/muliple_db.feature, features/portability.feature, .travis.yml, features/inheritence.feature
sample 3= features/relationship_indexing.feature, features/update.feature, features/append.feature, features/collection_proxy.feature, features/relationships.feature, features/basic.feature, features/muliple_db.feature, features/collection.feature, features/backward.feature, features/inheritence.feature
sample 4= features/persistence.feature, features/inheritence.feature, features/relationship_indexing.feature, features/collection_proxy.feature, features/append.feature, features/portability.feature, features/update.feature, features/collection.feature, .travis.yml, features/muliple_db.feature
----------------------
BBB:
sample 0= bbb-graphql-server/metadata/query_collections.yaml, bigbluebutton-web/grails-app/conf/spring/doc-conversion.xml, bbb-voice-conference/config/freeswitch/conf/autoload_configs/conference.conf.xml, docs/static/fonts/OpenSans-Medium.ttf, bbb-graphql-server/metadata/databases/BigBlueButton/tables/public_v_pres_page_cursor.yaml, docs/static/fonts/OpenSans-Bold.ttf, bigbluebutton-html5/public/locales/te.json, bigbluebutton-html5/public/locales/az.json, bigbluebutton-web/grails-app/i18n/messages_cs.properties, bigbluebutton-html5/public/locales/ml.json
sample 1= bbb-graphql-server/metadata/databases/BigBlueButton/tables/public_v_pres_page_writers.yaml, record-and-playback/video/scripts/playback-video.nginx, docs/static/fonts/OpenSans-Bold.ttf, bigbluebutton-web/grails-app/i18n/messages_it.properties, bbb-voice-conference/config/freeswitch/conf/autoload_configs/sndfile.conf.xml, bigbluebutton-html5/public/locales/es_MX.json, .idea/inspectionProfiles/Project_Default.xml, bbb-graphql-server/metadata/databases/BigBlueButton/tables/public_v_user_voice.yaml, record-and-playback/slides/scripts/slides.nginx, record-and-playback/core/resources/raw/chat_0_9.xml
sample 2= bigbluebutton-web/grails-app/i18n/messages_nl.properties, bigbluebutton-html5/public/locales/sv_SE.json, bbb-graphql-server/metadata/databases/BigBlueButton/tables/public_v_user_whiteboard.yaml, bigbluebutton-web/src/main/webapp/WEB-INF/tld/grails.tld, build/packages-template/bbb-etherpad/etherpad.service, record-and-playback/core/scripts/bbb-1.1-meeting-tag, record-and-playback/core/features/process_audio.feature, record-and-playback/core/resources/raw/unmatched_audio_events.xml, bigbluebutton-config/record/support-bbb-recording.patch, bbb-voice-conference/config/freeswitch/conf/autoload_configs/conference.conf.xml
sample 3= bbb-fsesl-client/src/test/resources/logback-test.xml, bbb-fsesl-client/src/main/resources/log4j.properties, bbb-graphql-server/metadata/databases/BigBlueButton/tables/public_v_user_typing_private.yaml, bigbluebutton-html5/public/locales/hr.json, bbb-learning-dashboard/learning-dashboard.nginx, bigbluebutton-html5/public/locales/vi_VN.json, bigbluebutton-html5/public/resources/tfmodels/segm_lite_v681.tflite, build/packages-template/bbb-webrtc-recorder/bbb-webrtc-recorder.service, bigbluebutton-web/grails-app/conf/spring/bbb-redis-messaging.xml, bigbluebutton-html5/public/locales/hy.json
sample 4= bigbluebutton-tests/playwright/.env.template, build/packages-template/bbb-webrtc-sfu/webrtc-sfu.nginx, bigbluebutton-html5/public/locales/ca.json, build/packages-template/bbb-export-annotations/bbb-export-annotations.service, record-and-playback/.rubocop.yml, bigbluebutton-web/grails-app/i18n/messages_es.properties, bigbluebutton-html5/public/locales/hr.json, bbb-graphql-server/metadata/cron_triggers.yaml, bbb-graphql-server/metadata/databases/BigBlueButton/tables/public_v_chat_message_public.yaml, bbb-graphql-server/metadata/databases/BigBlueButton/tables/public_v_meeting_usersPolicies.yaml
----------------------
CWA:
sample 0= services/distribution/src/test/resources/configtests/app-config_broken_syntax.yaml, services/distribution/src/test/resources/application-no-hour-retention.yaml, services/distribution/src/test/resources/parameters/weight_negative.yaml, .mvn/maven-version-rules.xml, services/download/src/test/resources/application-enable-test-data.yaml, common/shared/src/test/resources/validation_schema.json, services/distribution/src/main/resources/main-config/exposure-config.yaml, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/attenuation_duration.proto, services/distribution/src/main/resources/json/v1/cwa_reporting_public_data_region.json, services/distribution/src/main/resources/dgc/test-type.json
sample 1= runconfigs/IntelliJ/Submission Service.run.xml, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/dgc/dsc_list.proto, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/external/exposurenotification/temporary_exposure_key_signature_list.proto, .github/workflows/hadolint-analysis.yml, services/distribution/src/test/resources/testsetups/s3publishertest/topublish/file3.txt.checksum, runconfigs/Eclipse/spring-boot-dcc_revocation.launch, services/distribution/src/test/resources/testsetups/s3publishertest/topublish/file1.txt.checksum, docker-compose-test-secrets/contains_efgs_truststore.jks, services/distribution/src/main/resources/dgc/rule_3.json, services/upload/src/main/resources/application-e2e.yaml
sample 2= common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/v2/presence_tracing_parameters.proto, runconfigs/IntelliJ/distribution [spring-boot_run...].run.xml, services/distribution/src/test/resources/configtests/naming_mismatch.yaml, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/v2/dgc_parameters.proto, services/distribution/src/main/resources/trustList/ubirchDSC.json, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/stats/local_statistics.proto, services/download/src/test/resources/application-error-batch.yaml, services/distribution/src/main/resources/main-config/v2/exposure-configuration.yaml, runconfigs/Eclipse/spring-boot-dcc_revocation.launch, docker-compose-test-secrets/ssl.p12
sample 3= services/distribution/src/main/resources/main-config/v2/daily-summaries-config.yaml, runconfigs/Eclipse/spring-boot-submission.launch, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/v2/ppdd_ppa_parameters.proto, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/dgc/revocation_kid_list.proto, services/distribution/src/main/resources/dgc/bn_rule_1.json, .github/dependabot.yml, services/distribution/src/test/resources/configtests/exposure-config_ok.yaml, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/risk_score_classification.proto, services/distribution/src/main/resources/main-config/v2/plausible-deniability-parameters.yaml, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/external/exposurenotification/temporary_exposure_key_export.proto
sample 4= services/distribution/src/main/resources/main-config/v2/risk-calculation-parameters-1.15.yaml, services/upload/src/test/resources/testprivatekey.pem, common/shared/src/test/resources/validation_schema.json, services/distribution/src/main/resources/dgc/dcc-validation-service-allowlist-rule.json, services/distribution/src/main/resources/application-demo.yaml, runconfigs/IntelliJ/submission [spring-boot_run...].run.xml, services/distribution/src/main/resources/dgc/bn_rule_1.json, services/distribution/src/test/resources/application-allow-list-invalid.yaml, common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/v2/risk_calculation_parameters.proto, services/callback/src/test/resources/application-callback-change-certificate-cn.yaml
```
