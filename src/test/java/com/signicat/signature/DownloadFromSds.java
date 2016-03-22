package com.signicat.signature;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadFromSds {
    // After creating a PAdES using the packaging service,
    // the complete PAdES is available
    // for download from the Session Data Storage.

    @Test
    public void how_to_download_a_document_from_SDS() throws Exception {
        String padesDocumentId = "1803201653g2jc3jn5kpq4pnyv4x866odr31771x26c5zr757vl00bjc8t";

        String filename = padesDocumentId + "_" + new SimpleDateFormat("yyMMdd_HHmmss").format(new Date()) + ".pdf";

        Executor.newInstance()
                .auth("demo", "Bond007")
                .execute(Request.Get("https://preprod.signicat.com/doc/demo/sds/" + padesDocumentId))
                .saveContent(new File(filename));
    }
}
