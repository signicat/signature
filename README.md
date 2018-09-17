# Signature

Signicat Signatures Services is a collection of advanced web services that enables companies to utilize electronic signatures in business processes.

This project contains some unit tests and a very simple example web application. 
The Signature service API is explained in detail in the [DocumentService v3](https://developer.signicat.com/apis/documentservice/documentservice-v3/) section on developer.signicat.com. 
 
The simple web application in SignatureSample will start an embedded web server on port 4567. 
On accessing the main page on http://localhost:4567/, the user will enter the typical cycle of document signing:
 
1. Upload the documents to be signed to the Session Data Storage (SDS)
2. Create a request in the DocumentService
3. Redirect the user to Signicat for the signing request
4. Check Status of the request in DocumentService
5. Download the signed document from SDS
 
The example uses the *demo* service on *preprod.signicat.com*, and authentication is set up with *nemid*, but can easily be change to use other services or methods.
See individual ID method under [ID Methods](https://developer.signicat.com/id-methods/) for more information about obtaining test credentials.

For general information about the signature service, please refer to [Signing](https://developer.signicat.com/documentation/signing/)

## 2-Way SSL
For information about the process of setting up 2-way ssl, please refer to [Setting up 2-way SSL, Java](https://developer.signicat.com/documentation/ssl/setting-up-2-way-ssl-java/). 
