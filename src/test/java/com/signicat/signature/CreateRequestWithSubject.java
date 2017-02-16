package com.signicat.signature;

import com.signicat.document.v3.*;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.ws.Service;

public class CreateRequestWithSubject {

    // Use the DocumentService to create a document order request.
    // A document order may contain several documents, tasks and subjects (people).
    // This is a simple example where one subject must sign one document
    // and where the result is a plain NemID SDO (Signed Document Object)

    @Test
    public void how_to_create_a_simple_document_order_with_one_subject_and_one_document_using_Danish_NemID()
            throws Exception
    {
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

        // The document id is what you get in response when uploading a document to the SDS
        String documentId = "16022017bizpqo768j2a786et75hign2pa6aahb3nxxo80ap8q0s2zdis";

        CreateRequestRequest createRequestRequest = new CreateRequestRequest();
        createRequestRequest.setPassword("Bond007");
        createRequestRequest.setService("demo");

        Request request = new Request();
        request.setClientReference("cliref1");
        request.setLanguage("da");
        request.setProfile("demo");

        SdsDocument document = new SdsDocument();
        document.setId("doc_1");
        document.setRefSdsId(documentId);
        document.setDescription("Terms and conditions");
        request.getDocument().add(document);

        Subject subject = new Subject();
        subject.setId("subj_1");
        subject.setNationalId("1909740939");

        Task task = new Task();
        task.setId("task_1");
        task.setSubjectRef("subj_1");
        task.setBundle(false);

        DocumentAction documentAction = new DocumentAction();
        documentAction.setType(DocumentActionType.SIGN); // Sign or View
        documentAction.setDocumentRef("doc_1"); // Any identifier you'd like
        task.getDocumentAction().add(documentAction);

        Method signMethod = new Method();
        signMethod.setValue("nemid-sign"); // The name of the signature method(s)

        Signature signature = new Signature();
        signature.setResponsive(true);
        signature.getMethod().add(signMethod);
        task.getSignature().add(signature);

        request.getTask().add(task);

        createRequestRequest.getRequest().add(request);

        Service documentService = new DocumentService();
        DocumentEndPoint client = documentService.getPort(DocumentEndPoint.class);
        CreateRequestResponse response = client.createRequest(createRequestRequest);

        String signHereUrl =
                String.format("https://preprod.signicat.com/std/docaction/demo?request_id=%s&task_id=%s",
                        response.getRequestId().get(0), createRequestRequest.getRequest().get(0).getTask().get(0).getId());
        System.out.println(signHereUrl);
        Assert.assertNotNull(response);
        Assert.assertNull(response.getArtifact());
        Assert.assertNotNull(response.getRequestId());
    }
}
