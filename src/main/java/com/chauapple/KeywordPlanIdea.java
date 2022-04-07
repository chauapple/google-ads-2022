package com.chauapple;

//<editor-fold desc="IMPORT">

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v10.enums.KeywordPlanNetworkEnum;
import com.google.ads.googleads.v10.errors.GoogleAdsError;
import com.google.ads.googleads.v10.errors.GoogleAdsException;
import com.google.ads.googleads.v10.services.GenerateKeywordIdeaResult;
import com.google.ads.googleads.v10.services.GenerateKeywordIdeasRequest;
import com.google.ads.googleads.v10.services.KeywordPlanIdeaServiceClient;
import com.google.ads.googleads.v10.utils.ResourceNames;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//</editor-fold>

public class KeywordPlanIdea {

    public static void main(String[] args) throws IOException {
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

        long customerId = 8008776696l;
        long languageId = 1005l;

        //https://developers.google.com/google-ads/api/reference/data/geotargets
        List<Long> locationIds = new ArrayList<>();
        locationIds.add(1009023l);
        locationIds.add(1009024l);


        List<String> keywords = new ArrayList<>();
        keywords.add("sushi");


        try {
            new KeywordPlanIdea()
                    .runExample(
                            googleAdsClient,
                            customerId,
                            languageId,
                            locationIds,
                            keywords,
                            null);
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

    /**
     * Runs the example.
     *
     * @param googleAdsClient the Google Ads API client.
     * @param customerId      the client customer ID.
     * @param languageId      the language ID.
     * @param locationIds     the location IDs.
     * @param keywords        the list of keywords to use as a seed for ideas.
     * @param pageUrl         optional URL related to your business to use as a seed for ideas.
     * @throws GoogleAdsException       if an API request failed with one or more service errors.
     * @throws IllegalArgumentException if {@code keywords} is empty and {@code pageUrl} is null.
     * @throws Exception                if the example failed due to other errors.
     */
    private void runExample(
            GoogleAdsClient googleAdsClient,
            long customerId,
            long languageId,
            List<Long> locationIds,
            List<String> keywords,
            @Nullable String pageUrl) {
        try (KeywordPlanIdeaServiceClient keywordPlanServiceClient = googleAdsClient.getLatestVersion().createKeywordPlanIdeaServiceClient()) {
            GenerateKeywordIdeasRequest.Builder requestBuilder = GenerateKeywordIdeasRequest.newBuilder()
                    .setCustomerId(Long.toString(customerId))
                    // Sets the language resource using the provided language ID.
                    .setLanguage(ResourceNames.languageConstant(languageId))
                    // Sets the network. To restrict to only Google Search, change the parameter below to
                    // KeywordPlanNetwork.GOOGLE_SEARCH.
                    .setKeywordPlanNetwork(KeywordPlanNetworkEnum.KeywordPlanNetwork.GOOGLE_SEARCH_AND_PARTNERS);

            // Adds the resource name of each location ID to the request.
            for (Long locationId : locationIds) {
                requestBuilder.addGeoTargetConstants(ResourceNames.geoTargetConstant(locationId));
            }

            // Makes sure that keywords and/or page URL were specified. The request must have exactly one
            // of urlSeed, keywordSeed, or keywordAndUrlSeed set.
            if (keywords.isEmpty() && pageUrl == null) {
                throw new IllegalArgumentException(
                        "At least one of keywords or page URL is required, but neither was specified.");
            }

            if (keywords.isEmpty()) {
                // Only page URL was specified, so use a UrlSeed.
                requestBuilder.getUrlSeedBuilder().setUrl(pageUrl);
            } else if (pageUrl == null) {
                // Only keywords were specified, so use a KeywordSeed.
                requestBuilder.getKeywordSeedBuilder().addAllKeywords(keywords);
            } else {
                // Both page URL and keywords were specified, so use a KeywordAndUrlSeed.
                requestBuilder.getKeywordAndUrlSeedBuilder().setUrl(pageUrl).addAllKeywords(keywords);
            }

            // Sends the keyword ideas request.
            KeywordPlanIdeaServiceClient.GenerateKeywordIdeasPagedResponse response = keywordPlanServiceClient.generateKeywordIdeas(requestBuilder.build());
            // Prints each result in the response.
            for (GenerateKeywordIdeaResult result : response.iterateAll()) {
                System.out.printf(
                        "Keyword idea text '%s' has %d average monthly searches and '%s' competition.%n",
                        result.getText(),
                        result.getKeywordIdeaMetrics().getAvgMonthlySearches(),
                        result.getKeywordIdeaMetrics().getCompetition());
            }
        }
    }
}
