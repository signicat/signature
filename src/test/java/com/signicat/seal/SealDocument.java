package com.signicat.seal;

import java.io.File;
import java.io.IOException;

import javax.xml.ws.Service;

import org.apache.http.client.fluent.Executor;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;

import com.signicat.sealing.v1.CreateSealRequest;
import com.signicat.sealing.v1.CreateSealResponse;
import com.signicat.sealing.v1.DocumentId;
import com.signicat.sealing.v1.SdsDocumentId;
import com.signicat.sealing.v1.SealingEndPoint;
import com.signicat.sealing.v1.SealingService;

public class SealDocument {
    // Sealing a document can be done in three steps:
    // 1. Upload a PDF document to Session Data Storage (SDS).
    // 2. Use the SealingService to create the seal. The resulting document is available in SDS
    // 3. Download the sealed document
    @Test
    public void upload_document_to_sds_and_seal_it() throws Exception {
        SdsDocumentId sdsDocumentId = uploadDocument();

        CreateSealRequest request = getSealRequest(sdsDocumentId);
        Service sealingService = new SealingService();
        SealingEndPoint client = sealingService.getPort(SealingEndPoint.class);
        CreateSealResponse response = client.createSeal(request);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getId());

        String sealedDocumentUrl = "https://preprod.signicat.com/doc/demo/sds/" + response.getId();
        System.out.println(sealedDocumentUrl);
    }

    private SdsDocumentId uploadDocument() throws IOException {
        org.apache.http.client.fluent.Request request = org.apache.http.client.fluent.Request
                .Post("https://preprod.signicat.com/doc/demo/sds")
                .bodyFile(new File("src/test/resources/demo_avtale.pdf"), ContentType.create("application/pdf"));
        String documentId = Executor.newInstance()
                .auth("demo", "Bond007")
                .execute(request)
                .returnContent().asString();

        SdsDocumentId sdsDocumentId = new SdsDocumentId();
        sdsDocumentId.setId(documentId);
        return sdsDocumentId;
    }

    private CreateSealRequest getSealRequest(SdsDocumentId sdsDocumentId) {
        CreateSealRequest sealRequest = new CreateSealRequest();
        sealRequest.setService("demo");
        sealRequest.setPassword("Bond007");
        sealRequest.setPackagingMethod("seal");
        sealRequest.setVersion("1");

        DocumentId documentId = new DocumentId();
        documentId.setSdsDocumentId(sdsDocumentId);
        sealRequest.setSdo(documentId);

        return sealRequest;
    }
}
