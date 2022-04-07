package com.chauapple;

//<editor-fold desc="IMPORT">

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v10.errors.GoogleAdsError;
import com.google.ads.googleads.v10.errors.GoogleAdsException;
import com.google.ads.googleads.v10.services.ForecastMetrics;
import com.google.ads.googleads.v10.services.GenerateForecastMetricsResponse;
import com.google.ads.googleads.v10.services.KeywordPlanKeywordForecast;
import com.google.ads.googleads.v10.services.KeywordPlanServiceClient;
import com.google.ads.googleads.v10.utils.ResourceNames;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
//</editor-fold>

public class GenerateForecastMetrics {

    public static void main(String[] args) {
        GoogleAdsClient googleAdsClient = new GenerateForecastMetrics().connectAdsClient();

        long customerId = 8008776696l;
        long keywordPlanId = 389513461l;

        try {
            new GenerateForecastMetrics()
                    .runExample(googleAdsClient, customerId, keywordPlanId);
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

    /**
     * Runs the code example.
     *
     * @param googleAdsClient the Google Ads API client.
     * @param customerId      the client customer ID.
     * @param planId          the plan ID.
     */
    private void runExample(GoogleAdsClient googleAdsClient, Long customerId, Long planId) {
        String planResourceName = ResourceNames.keywordPlan(customerId, planId);

        try (KeywordPlanServiceClient client =
                     googleAdsClient.getLatestVersion().createKeywordPlanServiceClient()) {
            GenerateForecastMetricsResponse response = client.generateForecastMetrics(planResourceName);
            int i = 0;
            for (KeywordPlanKeywordForecast forecast : response.getKeywordForecastsList()) {
                ForecastMetrics metrics = forecast.getKeywordForecast();
                System.out.printf("%d Keyword ID: %s%n", ++i, forecast.getKeywordPlanAdGroupKeyword());
                System.out.printf("Estimated daily impressions: %f%n", metrics.getImpressions());
                System.out.printf("Estimated daily clicks: %f%n", metrics.getClicks());
                System.out.printf("Estimated daily CTR: %f%n", metrics.getCtr());
                System.out.printf("Estimated average cpc (micros): %d%n%n", metrics.getAverageCpc());
                System.out.printf("Estimated cost: %d%n%n", metrics.getCostMicros());
            }
        }
    }
}
