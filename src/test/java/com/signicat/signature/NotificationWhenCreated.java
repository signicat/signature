package com.signicat.signature;

import com.signicat.document.v2.*;
import org.apache.http.client.fluent.Executor;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.ws.Service;
import java.io.File;
import java.io.IOException;

public class NotificationWhenCreated {
    @Test
    public void notifications_can_be_sent_when_the_document_order_status_changes() throws Exception {
        SdsDocument uploadedDocument = uploadDocument();
        CreateRequestRequest request = getCreateRequest(uploadedDocument);

        // You can have request level notifications
        // for example to notify your system when
        // a request has been completed
        Notification requestNotification = new Notification();
        requestNotification.setNotificationId("req_not_1");
        requestNotification.setRecipient("https://labs.signicat.com/callback/");
        requestNotification.setMessage("Missing message");
        requestNotification.setType(NotificationType.URL);
        request.getRequest().get(0).getNotification().add(requestNotification);

        // And you can have task level notifications,
        // for example for notifying someone that
        // a document needs to be signed (or has been signed)
        Notification taskNotification = new Notification();
        taskNotification.setHeader("Attention required");
        taskNotification.setMessage("There are documents waiting for you to sign them. Please visit ${taskUrl}.");
        taskNotification.setNotificationId("req_not_2");
        taskNotification.setRecipient("support@signicat.com");
        taskNotification.setSender("noreply@signicat.com");
        taskNotification.setType(NotificationType.EMAIL);
        Schedule schedule = new Schedule();
        schedule.setStateIs(TaskStatus.CREATED);
        taskNotification.getSchedule().add(schedule);

        request.getRequest().get(0).getTask().get(0).getNotification().add(taskNotification);

        Service documentService = new DocumentService();
        DocumentEndPoint client = documentService.getPort(DocumentEndPoint.class);
        CreateRequestResponse response = client.createRequest(request);

        String signHereUrl =
                String.format("https://preprod.signicat.com/std/docaction/demo?request_id=%s&task_id=%s",
                        response.getRequestId().get(0), request.getRequest().get(0).getTask().get(0).getId());

        System.out.println(signHereUrl);
        Assert.assertNotNull(response.getRequestId());
    }

    private SdsDocument uploadDocument() throws IOException {
        org.apache.http.client.fluent.Request request = org.apache.http.client.fluent.Request
                .Post("https://preprod.signicat.com/doc/demo/sds")
                .bodyFile(new File("src/test/resources/demo_avtale.pdf"), ContentType.create("application/pdf"));
        String documentId = Executor.newInstance()
                .auth("demo", "Bond007")
                .execute(request)
                .returnContent().asString();

        SdsDocument sdsDocument = new SdsDocument();
        sdsDocument.setId("doc_1");
        sdsDocument.setRefSdsId(documentId);
        sdsDocument.setDescription("Terms and conditions");
        return sdsDocument;
    }


    private CreateRequestRequest getCreateRequest(SdsDocument documentInSds) {
        CreateRequestRequest createRequestRequest = new CreateRequestRequest();
        createRequestRequest.setService("demo");
        createRequestRequest.setPassword("Bond007");

        Request request = new Request();
        request.setClientReference("cliref1");
        request.setLanguage("da");
        request.setProfile("demo");

        request.getDocument().add(documentInSds);

        Task task = new Task();
        task.setId("task_1"); // Any identifier you'd like
        task.setSubjectRef("subj_1"); // Any identifier you'd like
        task.setBundle(false);

        DocumentAction documentAction = new DocumentAction();
        documentAction.setType(DocumentActionType.SIGN); // Sign or View
        documentAction.setDocumentRef("doc_1"); // Any identifier you'd like
        task.getDocumentAction().add(documentAction);

        Signature signature = new Signature();
        signature.getMethod().add("nemid-sign"); // The name of the signature method(s)
        task.getSignature().add(signature);

        request.getTask().add(task);

        createRequestRequest.getRequest().add(request);
        return createRequestRequest;
    }
}
