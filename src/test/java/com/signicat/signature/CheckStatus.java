package com.signicat.signature;

import com.signicat.document.v3.*;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.ws.Service;

public class CheckStatus {

    // The method getStatus can be used to inspect the status
    // of a document order and to get the result.
    // There are several mechanisms available to be notified
    // when a document order has changed, so
    // YOU MUST NEVER POLL THE GETSTATUS METHOD TO MONITOR A DOCUMENT ORDER.
    // Please see the other code examples of how to
    // notified of changes without polling.
    @Test
    public void how_to_check_the_status_and_result_of_a_document_order() {
        String requestId = "300820136vkdfvmxmh3f0ajx4ayaqfb25tsyfut2jptr3zz063dnm5kag";
        Service documentService = new DocumentService();
        DocumentEndPoint client = documentService.getPort(DocumentEndPoint.class);

        GetStatusRequest request = new GetStatusRequest();
        request.setPassword("Bond007");
        request.setService("demo");
        request.getRequestId().add(requestId);

        GetStatusResponse taskStatusInfo = client.getStatus(request);
        Assert.assertEquals(1, taskStatusInfo.getTaskStatusInfo().size());
        Assert.assertEquals(TaskStatus.COMPLETED, taskStatusInfo.getTaskStatusInfo().get(0).getTaskStatus());
        Assert.assertEquals("doc_1", taskStatusInfo.getTaskStatusInfo().get(0).getDocumentStatus().get(0).getId());

        String taskId = "task_1";
        String documentId = "doc_1";
        String originalUri = String.format("https://preprod.signicat.com/doc/demo/order/%s/%s/%s/original", requestId, taskId, documentId);
        String resultUri = String.format("https://preprod.signicat.com/doc/demo/order/%s/%s/%s/sdo", requestId, taskId, documentId);

        Assert.assertEquals(originalUri, taskStatusInfo.getTaskStatusInfo().get(0).getDocumentStatus().get(0).getOriginalUri());
        Assert.assertEquals(resultUri, taskStatusInfo.getTaskStatusInfo().get(0).getDocumentStatus().get(0).getResultUri());
        System.out.println("original=" + originalUri);
        System.out.println("result=" + resultUri);
    }
}
