package com.signicat.signature;

import com.signicat.packaging.v4.*;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.ws.Service;

public class CreatePadesFromLtvSdo {
    // Given one or more LTV SDO's, you may invoke the packaging service
    // to create a PAdES containing the signed documents.
    // You may create your own PDF front page template containing
    // custom graphics, text and watermarks.
    // The following is a simple example, please contact support@signicat.com
    // or consult https://support.signicat.com/download/attachments/10716012/Customizing-PAdES-with-templates-2.1%20%282%29.pdf?version=1&modificationDate=1415100520306&api=v2
    // for more information
    @Test
    public void how_to_use_the_packaging_service_to_create_a_PAdES_from_an_LtvSdo() throws Exception {
        String resultSdoUri = "https://preprod.signicat.com/doc/demo/order/3008201345nxiv5g9tnofl7qqgf56p9kr27gl6gxfjf5jqdwwxx984wj48/task_1/doc_1/sdo";

        Service packagingService = new PackagingService();
        PackagingEndPoint client = packagingService.getPort(PackagingEndPoint.class);

        CreatePackageRequest request = new CreatePackageRequest();
        request.setService("demo");
        request.setPassword("Bond007");
        request.setVersion("4");
        request.setPackagingMethod("pades");
        request.setValidationPolicy("ltvsdo-validator");

        DocumentId documentId = new DocumentId();
        documentId.setUriDocumentId(resultSdoUri);
        request.getSdoOrSdoExtended().add(documentId);
        request.setSendResultToArchive(false);

        CreatePackageResponse createPackageResponse = client.createPackage(request);
        String padesDocumentId = createPackageResponse.getId();
        String padesDownloadUrl = "https://preprod.signicat.com/doc/demo/sds/" + padesDocumentId;
        // if you set sendresulttoarchive=true, the url must also be updated:
        //string padesDownloadUrl = "https://preprod.signicat.com/doc/demo/archive/" + padesDocumentId;

        System.out.println("documentId=" + padesDocumentId);
        System.out.println(padesDownloadUrl);
        Assert.assertNull(createPackageResponse.getError());
        Assert.assertNotNull(padesDocumentId);
    }
}
