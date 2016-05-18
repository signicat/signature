package com.signicat.sds;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UploadSds {
    @Test
    public void uploading_a_pdf_to_sds_using_apache_http_client() throws IOException {
        // Create the client and set it up for basic authentication
        AuthScope authScope = new AuthScope("preprod.signicat.com", 443);
        Credentials credentials = new UsernamePasswordCredentials("demo", "Bond007");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(authScope, credentials);
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();

        // Grab the PDF file and add it to the HttpPost request
        HttpPost post = new HttpPost("https://preprod.signicat.com/doc/demo/sds");
        File pdf = new File("src/test/resources/demo_avtale.pdf");
        post.setEntity(new FileEntity(pdf, ContentType.create("application/pdf")));

        // Execute the request and read the document id from the response
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = rd.readLine();

        assertNotNull(line);
        assertEquals(58, line.length());
    }
}
