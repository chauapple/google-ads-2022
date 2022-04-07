package com.chauapple;

//<editor-fold desc="IMPORT">

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v10.enums.KeywordMatchTypeEnum;
import com.google.ads.googleads.v10.enums.KeywordPlanForecastIntervalEnum;
import com.google.ads.googleads.v10.enums.KeywordPlanNetworkEnum;
import com.google.ads.googleads.v10.errors.GoogleAdsError;
import com.google.ads.googleads.v10.errors.GoogleAdsException;
import com.google.ads.googleads.v10.resources.*;
import com.google.ads.googleads.v10.services.*;
import com.google.ads.googleads.v10.utils.ResourceNames;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.chauapple.CodeSampleHelper.getPrintableDateTime;
//</editor-fold>

public class AddKeywordPlan {

    public static void main(String[] args) throws IOException {
        GoogleAdsClient googleAdsClient = new AddKeywordPlan().connectAdsClient();
        long customerId = 8141845268l;

        try {
            new AddKeywordPlan()
                    .runExample(googleAdsClient, customerId);
        } catch (GoogleAdsException gae) {
            // GoogleAdsException is the base class for most exceptions thrown by an API request.
            // Instances of this exception have a message and a GoogleAdsFailure that contains a
            // collection of GoogleAdsErrors that indicate the underlying causes of the
            // GoogleAdsException.
            System.err.printf(
                    "Request ID %s failed due to GoogleAdsException. Underlying errors:%n",
                    gae.getRequestId());
            int i = 0;
            for (GoogleAdsError googleAdsError : gae.getGoogleAdsFailure().getErrorsList()) {
                System.err.printf("  Error %d: %s%n", i++, googleAdsError);
            }
            System.exit(1);
        }
    }

    private void runExample(GoogleAdsClient googleAdsClient, Long customerId) {
        List<String> keywordList = new ArrayList<>();
        keywordList.add("成増 寿司");
        keywordList.add("宮古 寿司");

        String keywordPlanResource = createKeywordPlan(googleAdsClient, customerId);
        String planCampaignResource = createKeywordPlanCampaign(googleAdsClient, customerId, keywordPlanResource);
        String planAdGroupResource = createKeywordPlanAdGroup(googleAdsClient, customerId, planCampaignResource);
        createKeywordPlanAdGroupKeywords(googleAdsClient, customerId, planAdGroupResource, keywordList);
        //createKeywordPlanCampaignKeywords(googleAdsClient, customerId, planCampaignResource);
    }

    //<editor-fold desc="CONNECT ADS CLIENT">
    private GoogleAdsClient connectAdsClient() {
        GoogleAdsClient googleAdsClient = null;
        try {
            ClassLoader classLoader = KeywordPlanIdea.class.getClassLoader();
            File file = new File(classLoader.getResource("ads.properties").getFile());
            googleAdsClient = GoogleAdsClient.newBuilder().fromPropertiesFile(file).build();
        } catch (FileNotFoundException fnfe) {
            System.err.printf(
                    "Failed to load GoogleAdsClient configuration from file. Exception: %s%n", fnfe);
            System.exit(1);
        } catch (IOException ioe) {
            System.err.printf("Failed to create GoogleAdsClient. Exception: %s%n", ioe);
            System.exit(1);
        }
        return googleAdsClient;
    }
    //</editor-fold>

    //<editor-fold desc="KEYWORD PLAN">

    /**
     * Creates a keyword plan.
     *
     * @param googleAdsClient the Google Ads API client.
     * @param customerId      the client customer ID.
     */
    private static String createKeywordPlan(GoogleAdsClient googleAdsClient, Long customerId) {
        KeywordPlan plan = KeywordPlan.newBuilder()
                .setName("Keyword plan for traffic estimate #" + getPrintableDateTime())
                .setForecastPeriod(
                        KeywordPlanForecastPeriod.newBuilder()
                                .setDateInterval(KeywordPlanForecastIntervalEnum.KeywordPlanForecastInterval.NEXT_MONTH)
                                .build())
                .build();

        KeywordPlanOperation op = KeywordPlanOperation.newBuilder().setCreate(plan).build();

        try (KeywordPlanServiceClient client =
                     googleAdsClient.getLatestVersion().createKeywordPlanServiceClient()) {
            // Adds the keyword plan.
            MutateKeywordPlansResponse response =
                    client.mutateKeywordPlans(String.valueOf(customerId), Arrays.asList(op));

            // Displays the results.
            String resourceName = response.getResults(0).getResourceName();
            System.out.printf("Created keyword plan: %s%n", resourceName);
            return resourceName;
        }
    }
    //</editor-fold>

    //<editor-fold desc="KEYWORD PLAN CAMPAIGN">

    /**
     * Creates a campaign for the keyword plan.
     *
     * @param googleAdsClient     the Google Ads API client.
     * @param customerId          the client customer ID.
     * @param keywordPlanResource the keyword plan resource name.
     */
    private static String createKeywordPlanCampaign(
            GoogleAdsClient googleAdsClient, Long customerId, String keywordPlanResource) {
        // Creates a keyword plan campaign.
        KeywordPlanCampaign.Builder campaign =
                KeywordPlanCampaign.newBuilder()
                        .setName("Keyword plan campaign #" + getPrintableDateTime())
                        //Try setting the maximum cost per click with the default value (260 yen) when creating on the screen
                        //This setting can be set for each ad group or individual keyword instead of campaign
                        .setCpcBidMicros(260000000) // 1 yen = 1,000,000 (1000000 because the unit is micro)
                        .setKeywordPlanNetwork(KeywordPlanNetworkEnum.KeywordPlanNetwork.GOOGLE_SEARCH)
                        .setKeywordPlan(keywordPlanResource);

        // See https://developers.google.com/google-ads/api/reference/data/geotargets
        // for the list of geo target IDs.
        campaign.addGeoTargets(
                KeywordPlanGeoTarget.newBuilder()
                        .setGeoTargetConstant(ResourceNames.geoTargetConstant(2392)) //JP
                        .build());

        // See https://developers.google.com/google-ads/api/reference/data/codes-formats#languages
        // for the list of language criteria IDs.
        campaign.addLanguageConstants(ResourceNames.languageConstant(1005)); //JA

        KeywordPlanCampaignOperation op =
                KeywordPlanCampaignOperation.newBuilder().setCreate(campaign).build();

        try (KeywordPlanCampaignServiceClient client =
                     googleAdsClient.getLatestVersion().createKeywordPlanCampaignServiceClient()) {
            // Adds the campaign.
            MutateKeywordPlanCampaignsResponse response =
                    client.mutateKeywordPlanCampaigns(String.valueOf(customerId), Arrays.asList(op));

            // Displays the result.
            String resourceName = response.getResults(0).getResourceName();
            System.out.printf("Created campaign for keyword plan: %s%n", resourceName);
            return resourceName;
        }
    }
    //</editor-fold>

    //<editor-fold desc="KEYWORD PLAN ADGROUP">

    /**
     * Creates the ad group for the keyword plan.
     *
     * @param googleAdsClient      the Google Ads API client.
     * @param customerId           the client customer ID.
     * @param planCampaignResource plan campaign resource name.
     */
    private static String createKeywordPlanAdGroup(
            GoogleAdsClient googleAdsClient, Long customerId, String planCampaignResource) {
        // Creates the keyword plan ad group.
        KeywordPlanAdGroup.Builder adGroup =
                KeywordPlanAdGroup.newBuilder()
                        .setKeywordPlanCampaign(planCampaignResource)
                        .setName("Keyword plan ad group #" + getPrintableDateTime())
                        .setCpcBidMicros(1000000);

        KeywordPlanAdGroupOperation op =
                KeywordPlanAdGroupOperation.newBuilder().setCreate(adGroup).build();
        try (KeywordPlanAdGroupServiceClient client =
                     googleAdsClient.getLatestVersion().createKeywordPlanAdGroupServiceClient()) {
            // Adds the ad group.
            MutateKeywordPlanAdGroupsResponse response =
                    client.mutateKeywordPlanAdGroups(String.valueOf(customerId), Arrays.asList(op));

            // Displays the result.
            String resourceName = response.getResults(0).getResourceName();
            System.out.println("Created ad group for keyword plan: " + resourceName);
            return resourceName;
        }
    }
    //</editor-fold>

    //<editor-fold desc="KEYWORD FOR KEYWORD PLAN">

    /**
     * Creates keywords for the keyword plan.
     *
     * @param googleAdsClient     the Google Ads API client.
     * @param customerId          the client customer ID.
     * @param planAdGroupResource plan ad group resource name.
     */
    private static void createKeywordPlanAdGroupKeywords(
            GoogleAdsClient googleAdsClient, Long customerId, String planAdGroupResource, List<String> keywordList) {
        List<KeywordPlanAdGroupKeywordOperation> operations = new ArrayList<>();
        for (String eachKeyword : keywordList) {
            KeywordPlanAdGroupKeyword keyword =
                    KeywordPlanAdGroupKeyword.newBuilder()
                            .setKeywordPlanAdGroup(planAdGroupResource)
                            .setCpcBidMicros(1000000)
                            .setMatchType(KeywordMatchTypeEnum.KeywordMatchType.EXACT)
                            .setText(eachKeyword)
                            .build();
            operations.add(KeywordPlanAdGroupKeywordOperation.newBuilder().setCreate(keyword).build());
        }
        try (KeywordPlanAdGroupKeywordServiceClient client =
                     googleAdsClient.getLatestVersion().createKeywordPlanAdGroupKeywordServiceClient()) {
            // Adds the keywords.
            MutateKeywordPlanAdGroupKeywordsResponse response =
                    client.mutateKeywordPlanAdGroupKeywords(String.valueOf(customerId), operations);
            // Displays the results.
            for (MutateKeywordPlanAdGroupKeywordResult result : response.getResultsList()) {
                System.out.printf("Created keyword for keyword plan: %s%n", result.getResourceName());
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="KEYWORD FOR KEYWORD PLAN (NEGATIVE)">

    /**
     * Creates negative keywords for the keyword plan.
     *
     * @param googleAdsClient      the Google Ads API client.
     * @param customerId           the client customer ID.
     * @param planCampaignResource plan campaign resource name.
     */
    private void createKeywordPlanCampaignKeywords(
            GoogleAdsClient googleAdsClient, Long customerId, String planCampaignResource) {
        KeywordPlanCampaignKeyword negativeKeyword =
                KeywordPlanCampaignKeyword.newBuilder()
                        .setKeywordPlanCampaign(planCampaignResource)
                        .setMatchType(KeywordMatchTypeEnum.KeywordMatchType.BROAD)
                        .setNegative(true)
                        .setText("moon walk")
                        .build();
        KeywordPlanCampaignKeywordOperation op =
                KeywordPlanCampaignKeywordOperation.newBuilder().setCreate(negativeKeyword).build();

        try (KeywordPlanCampaignKeywordServiceClient client =
                     googleAdsClient.getLatestVersion().createKeywordPlanCampaignKeywordServiceClient()) {
            // Adds the negative keyword.
            MutateKeywordPlanCampaignKeywordsResponse response =
                    client.mutateKeywordPlanCampaignKeywords(String.valueOf(customerId), Arrays.asList(op));

            // Displays the result.
            String resourceName = response.getResults(0).getResourceName();
            System.out.printf("Created negative keyword for keyword plan: %s%n", resourceName);
        }
    }
    //</editor-fold>

}
