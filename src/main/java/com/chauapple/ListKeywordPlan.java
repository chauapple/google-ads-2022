package com.chauapple;

//<editor-fold desc="IMPORT">

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v10.errors.GoogleAdsError;
import com.google.ads.googleads.v10.errors.GoogleAdsException;
import com.google.ads.googleads.v10.services.GoogleAdsRow;
import com.google.ads.googleads.v10.services.GoogleAdsServiceClient;
import com.google.ads.googleads.v10.services.SearchGoogleAdsRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
//</editor-fold>

public class ListKeywordPlan {

    public static void main(String[] args) {
        GoogleAdsClient googleAdsClient = new ListKeywordPlan().connectAdsClient();
        long customerId = 8008776696l;
        try {
            new ListKeywordPlan()
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

    private void runExample(GoogleAdsClient googleAdsClient, long customerId) {
        GoogleAdsServiceClient client = googleAdsClient.getLatestVersion().createGoogleAdsServiceClient();
        GoogleAdsServiceClient.SearchPagedResponse response = client.search(SearchGoogleAdsRequest.newBuilder()
                .setQuery("SELECT keyword_plan.resource_name FROM keyword_plan")
                .setCustomerId(String.valueOf(customerId))
                .build());

        List<GoogleAdsRow> results = response.getPage().getResponse().getResultsList();
        System.out.printf("Total row: %d%n", results.size());
        for (GoogleAdsRow eachRow : results) {
            System.out.printf("Resource name: %s%n", eachRow.getKeywordPlan().getResourceName());
        }
    }
}
