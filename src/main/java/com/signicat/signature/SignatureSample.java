package com.signicat.signature;

import com.signicat.document.v3.*;

import org.apache.http.client.fluent.Executor;
import org.apache.http.entity.ContentType;
import spark.Spark;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static spark.Spark.get;
import static spark.Spark.post;

public class SignatureSample {

    public static final String SIGNICAT_URL = "https://preprod.signicat.com";
    public static final String SERVICE = "demo";
    public static final String METHOD = "nemid-sign";

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

        get("/download", (request, response) -> {
            String orderUrl = request.queryParams("order");
            org.apache.http.client.fluent.Request req = org.apache.http.client.fluent.Request
                    .Get(orderUrl);
            byte[] bytes = Executor.newInstance().auth(SERVICE, "Bond007")
            .execute(req).returnContent().asBytes();
            response.header("Content-Type", "application/pdf");
            response.header("Content-Disposition", "attachment;filename=\"file.pdf\"");
            response.raw().getOutputStream().write(bytes);
            response.raw().getOutputStream().flush();
            response.raw().getOutputStream().close();
            return null;
        });
    }

    private static String uploadDocument(byte[] file) throws IOException {
        // Upload file to SDS
        org.apache.http.client.fluent.Request request = org.apache.http.client.fluent.Request
                .Post(SIGNICAT_URL + "/doc/" + SERVICE + "/sds")
                .bodyByteArray(file, ContentType.create("application/pdf"));
        return Executor.newInstance()
                .auth(SERVICE, "Bond007")  // Basic auth
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

        Method signMethod = new Method();
        signMethod.setValue(METHOD);

        Signature signature = new Signature();
        signature.setResponsive(true);
        signature.getMethod().add(signMethod);
        task.getSignature().add(signature);

        SdsDocument sdsDocument = new SdsDocument();
        sdsDocument.setId(docReference);
        sdsDocument.setRefSdsId(documentId);
        sdsDocument.setDescription("My document");

        Request request = new Request();
        request.setLanguage("da");
        request.setProfile(SERVICE);
        request.getTask().add(task);
        request.getDocument().add(sdsDocument);

        CreateRequestRequest createRequestRequest = new CreateRequestRequest();
        createRequestRequest.setService(SERVICE);
        createRequestRequest.setPassword("Bond007");
        createRequestRequest.getRequest().add(request);

        DocumentEndPoint documentEndPoint = getDocumentEndPoint();
        CreateRequestResponse createRequestResponse = documentEndPoint.createRequest(createRequestRequest);

        // Request returned requestId - now redirect to signicat for signing
        return String.format(SIGNICAT_URL + "/std/docaction/" + SERVICE + "?request_id=%s&task_id=%s",
                createRequestResponse.getRequestId().get(0), createRequestRequest.getRequest().get(0).getTask().get(0).getId());
    }

    private static String checkStatus(String requestId, String taskId, String docReference) {
        GetStatusRequest request = new GetStatusRequest();
        request.setPassword("Bond007");
        request.setService(SERVICE);
        request.getRequestId().add(requestId);

        DocumentEndPoint documentEndPoint = getDocumentEndPoint();
        GetStatusResponse taskStatusInfo = documentEndPoint.getStatus(request);

        if (taskStatusInfo.getTaskStatusInfo().get(0).getTaskStatus() == TaskStatus.COMPLETED) {
            // Signature completed - return url to signed document
            return String.format(SIGNICAT_URL + "/doc/" + SERVICE + "/order/%s/%s/%s/sdo", requestId, taskId, docReference);
        }
        return null;
    }

    private static DocumentEndPoint getDocumentEndPoint() {
        Service documentService = new DocumentService();
        DocumentEndPoint port = documentService.getPort(DocumentEndPoint.class);
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, SIGNICAT_URL + "/ws/documentservice-v3");

        return port;
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
