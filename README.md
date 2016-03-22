# Signature

Signicat Signatures Services is a collection of advanced web services that enables companies to utilize electronic signatures in business processes.

This project contains some unit tests and a very simple example web application. 
The tests are explained in the [DocumentService](https://support.signicat.com/display/S2/DocumentService) section on support.signicat.com. 
 
The simple web application in SignatureSample will start an embedded web server on port 4567. 
On accessing the main page on http://localhost:4567/, the user will enter the typical cycle of document signing:
 
1. Upload the documents to be signed to the Session Data Storage (SDS)
2. Create a request in the DocumentService
3. Redirect the user to Signicat for the signing request
4. Check Status of the request in DocumentService
5. Download the signed document from SDS
 
The example uses the *demo* service on *preprod.signicat.com*, and authentication is set up with *nemid*, but can easily be change to use other services or methods.
See [Get test users and test information](https://support.signicat.com/display/S2/Get+test+users+and+test+information) for more information about obtaining test credentials.

For general information about the signature service, please refer to [Getting started with signatures](https://support.signicat.com/display/S2/Getting+started+with+signatures)
