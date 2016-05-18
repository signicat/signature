package com.signicat.sds;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DownloadSds {
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

        // Construct the URL to the document and add it to the HttpGet request
        String documentId = "180520162syjw99j2seogz60o6kls2hqtt3obbdpbym1bw8w9po0y86iyh";
        String sdsUrl = "https://preprod.signicat.com/doc/demo/sds/" + documentId;
        HttpGet get = new HttpGet(sdsUrl);

        // Execute the request and read+save the document id from the response
        HttpResponse response = client.execute(get);
        File pdf = new File("mydownloadedfile.pdf");
        FileOutputStream out = new FileOutputStream(pdf);

        HttpEntity entity = response.getEntity();
        entity.writeTo(out);
        out.close();

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(response.getFirstHeader("Content-Type").getValue().startsWith("application/pdf"));
    }
}
