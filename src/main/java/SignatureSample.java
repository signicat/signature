import com.signicat.document.v2.*;
import org.apache.http.client.fluent.Executor;
import org.apache.http.entity.ContentType;
import spark.Spark;

import javax.xml.ws.Service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static spark.Spark.get;
import static spark.Spark.post;

public class SignatureSample {

    public static void main(String[] args) {
        // Print all HTTP request/response to system.out
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");

        // Setting up 2-way SSL
        System.setProperty("javax.net.ssl.keyStore","src/main/resources/web-service-keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","changeit");
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");

        Spark.staticFileLocation("/public");

        post("/upload", (request, response) -> uploadDocument(request.bodyAsBytes()));

        get("/sign", (request, response) -> {
            String signHereUrl = createRequest(request.queryParams("document_id"), request.url());
            response.redirect(signHereUrl);
            return null;
        });

        get("/complete", (request, response) -> {
            String docReference = request.queryParams("docRef");
            String requestId = request.queryParams("requestId");
            String taskId = request.queryParams("taskId");
            String downloadUrl = urlEncode(checkStatus(requestId, taskId, docReference));
            response.redirect("/index.html?downloadUrl=" + downloadUrl);
            return null;
        });
    }

    private static String uploadDocument(byte[] file) throws IOException {
        // Upload file to SDS
        org.apache.http.client.fluent.Request request = org.apache.http.client.fluent.Request
                .Post("https://preprod.signicat.com/doc/demo/sds")
                .bodyByteArray(file, ContentType.create("application/pdf"));
        return Executor.newInstance()
                .auth("demo", "Bond007")  // Basic auth
                .execute(request)
                .returnContent().asString();
    }

    private static String createRequest(String documentId, String url) {
        String docReference = "doc_1";

        Task task = new Task();
        task.setId("task_1");
        // Add callback url for redirection after signature completion
        task.setOnTaskComplete(url + "/../complete?status=complete&docRef=" + docReference + "&requestId=${requestId}&taskId=${taskId}");
        task.setBundle(false);

        DocumentAction documentAction = new DocumentAction();
        documentAction.setType(DocumentActionType.SIGN);
        documentAction.setDocumentRef(docReference);
        task.getDocumentAction().add(documentAction);

        Signature signature = new Signature();
        signature.getMethod().add("nemid-sign");
        task.getSignature().add(signature);

        SdsDocument sdsDocument = new SdsDocument();
        sdsDocument.setId(docReference);
        sdsDocument.setRefSdsId(documentId);
        sdsDocument.setDescription("My document");

        Request request = new Request();
        request.setLanguage("da");
        request.setProfile("demo");
        request.getTask().add(task);
        request.getDocument().add(sdsDocument);

        CreateRequestRequest createRequestRequest = new CreateRequestRequest();
        createRequestRequest.setService("demo");
        createRequestRequest.setPassword("Bond007");
        createRequestRequest.getRequest().add(request);

        DocumentEndPoint documentEndPoint = getDocumentEndPoint();
        CreateRequestResponse createRequestResponse = documentEndPoint.createRequest(createRequestRequest);

        // Request returned requestId - now redirect to signicat for signing
        return String.format("https://preprod.signicat.com/std/docaction/demo?request_id=%s&task_id=%s",
                createRequestResponse.getRequestId().get(0), createRequestRequest.getRequest().get(0).getTask().get(0).getId());
    }

    private static String checkStatus(String requestId, String taskId, String docReference) {
        GetStatusRequest request = new GetStatusRequest();
        request.setPassword("Bond007");
        request.setService("demo");
        request.getRequestId().add(requestId);

        DocumentEndPoint documentEndPoint = getDocumentEndPoint();
        GetStatusResponse taskStatusInfo = documentEndPoint.getStatus(request);

        if (taskStatusInfo.getTaskStatusInfo().get(0).getTaskStatus() == TaskStatus.COMPLETED) {
            // Signature completed - return url to signed document
            return String.format("https://preprod.signicat.com/doc/demo/order/%s/%s/%s/sdo", requestId, taskId, docReference);
        }
        return null;
    }

    private static DocumentEndPoint getDocumentEndPoint() {
        Service documentService = new DocumentService();
        return documentService.getPort(DocumentEndPoint.class);
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
