## Hearth Application

Hearth is a backend server application.

It provides the operations required to manage application resources and verify the integrity of server responses.
 
## Requirements

Java 17 or higher and Maven 3.x are required.


## Table of Contents

- [Requirements](#requirements)
- [Getting Started](#getting-started)
  - [Check Out the Code](#check-out-the-code)
  - [Compile the Code](#compile-the-code)
  - [Version Upgrade](#version-upgrade)
  - [Local Deployment](#local-deployment)
- [Review the Swagger Documentation](#review-the-swagger-documentation)
- [SSL Configuration](#ssl-configuration)
  - [Server Configuration](#server-configuration)
  - [Keys and Certificates](#keys-and-certificates)
- [Testing Hearth Application](#testing-hearth-application)
  - [Obtain an Admin Token](#step-1---obtain-an-admin-token)
  - [Create a User](#step-2---create-a-user)
  - [Generate a User Token (Non-Admin)](#step-3---generate-a-user-token-non-admin)
  - [View Own Profile](#step-4---view-own-profile)
- [Keystore Handling](#keystore-handling)
  - [Set Up the Browser-Facing Certificate for Node.js](#set-up-the-browser-facing-certificate-for-nodejs)
  - [Setup mTLS](#setup-mtls)
  - [Generate Keystore for JWT Signing and Validation](#generate-keystore-for-jwt-signing-and-validation)
  - [View the Keystore](#view-the-keystore)
  - [Additional Options](#additional-options)
- [Test the Service](#test-the-service)
- [Configuration](#configuration)



## Getting Started

This sample application provides a REST API using [Declarative Vert](https://github.com/javalabs-eng/declarative-vertx), so it is very easy to make it work as standalone server.  

### Check Out the Code

```
<prompt> git clone https://github.com/hearth-eng/hearth-app

```

### Compile the Code

`hearth-app` uses Maven as its build tool. Use the following command to compile the codebase. JDK 17 or later is required.

```
<prompt> mvn clean install

```

### Version Upgrade

To update the module version in the parent POM and all child modules, run the following command:

```
<prompt> mvn versions:set -DnewVersion=YOUR_NEW_VERSION

```

### Local Deployment

#### Prerequisites

The Hearth backend requires PostgreSQL. Follow the [Hearth DB](https://github.com/hearth-eng/hearth-db) instructions to install PostgreSQL and set up the Hearth schema.

#### Start Hearth Server

Once the database setup is complete, run `start.sh` to start the Hearth backend server.

```
<prompt> sh start.sh

```

The application should start and have an output similar to this: 

    [...]
    [vert.x-eventloop-thread-1] INFO org.javalabs.decl.vertx.container.VertxHttpServer - Started Http Server. Listening to port: 8080
    [main] INFO org.javalabs.decl.vertx.container.VertxContainer - Deployment of verticle app.http.server is successful. Deployment Id: 3abd2b35-4cf7-426f-83a9-7b394710df08
    [vert.x-worker-thread-0] INFO com.hearth.app.core.AppProcessor - Scheduled default timer. Initial Delay: 0. Pause Time (ms): 1800000
    [vert.x-worker-thread-0] INFO com.hearth.app.core.AppProcessor - Started Verticle: AppProcessor
    [main] INFO org.javalabs.decl.vertx.container.VertxContainer - Deployment of verticle app.processor is successful. Deployment Id: 04595511-d20a-43e2-abda-52c931c2d531


The server port is configured in `server.xml`. Update that configuration if you need to use a different port.

## Review the Swagger Documentation

The `openapi.yaml` file is generated in:

```
docs/openapi.yaml
```

To view the Swagger documentation, navigate to the `docs` directory and run the following command to start a simple HTTP server:
```
<prompt> python3 -m http.server -b 127.0.0.1
```

The server starts and displays output similar to:
```
<prompt> Serving HTTP on 127.0.0.1 port 8000 (http://127.0.0.1:8000/) ...
```

Open `http://127.0.0.1:8000/` in your browser to view the API documentation.


## SSL Configuration

### Server Configuration

`hearth-app` is configured to start with SSL enabled. Refer to the following snippet from `server.xml`:

```
<server-config>
    <server-opts>
    	<port>9443</port>
        <client-auth>REQUIRED</client-auth>
    </server-opts>
    <tcp-opts>
        <ssl>true</ssl>
    </tcp-opts>
    ...
    ...
    ...

</server-config>

```

<client-auth>`REQUIRED`</client-auth> requires clients, such as cURL or Postman, to present a valid client certificate. This enables mTLS between the client and `hearth-app`.

### Keys and Certificates

`hearth-app` includes its server key, certificates, and CA certificate under `src/main/resources`.

```
src/main/resources
      |
       --- ca
      |     |
      |      --- ca_javalabs.key
      |     |
      |      --- ca_javalabs.crt
      |
       --- server_cert
      |     |
      |      --- hearth-app.key
      |     |
      |      --- hearth-app.crt
      |
       --- client_cert
            |
             --- hearth-client.key
            |
             --- hearth-client.crt

```

The `client_cert` directory is not used as the server identity. Its certificate and key are used by clients when establishing mTLS connections, including requests used to obtain authentication tokens.

## Testing Hearth Application

Because `hearth-app` requires SSL and mTLS, a client must present a valid client certificate when establishing a connection before making API calls.

### Step 1 - Obtain an Admin Token

An admin token is required for certain operations, for example:

1. Creating a User         [ scope -> user:create ]
2. Viewing All Users    [ scope -> user:query ]

We will create a token with the scope `user:create`. Likewise, create a token with scope `user:query` if you want to view all users.

```
curl -i \
    -X POST \
    -u '9efbd3b3-a0a9-468a-8652-7f489adf6a45:7c6a180b36896a0a8c02787eeafb0e4c' \
    --cert /path/to/hearth-app/src/main/resources/client_cert/hearth-client.crt \
    --key /path/to/hearth-app/src/main/resources/client_cert/hearth-client.key \
    --cacert /path/to/hearth-app/src/main/resources/ca/ca_javalabs.crt \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d 'grant_type=client_credentials&scope=user%3Acreate' \
    https://localhost:9443/api/v1/mgmt/login

```

Response:

```
{
  "token_type" : "Bearer",
  "access_token" : "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI5ZWZiZDNiMy1hMGE5LTQ2OGEtODY1Mi03ZjQ4OWFkZjZhNDUiLCJhdWQiOm51bGwsInNjb3BlIjoidXNlcjpjcmVhdGUiLCJpc3MiOiJmb2xrcyIsInByaXYiOiJhZG1pbiIsImp0aSI6ImM5YmVhNDQzLTJlOGEtNDNiNi1hMzY1LTBmN2FiYjdiMmRjZCIsImlhdCI6MTc4Njc4NjAwOCwiZXhwIjoxNzg2Nzg5NjA4fQ.Tej28BCxo3GG6KsabNW_Q82Q_o6h5a9XSefjXe0WBLVI4hIuA2-_dA2zNh1ae3mEkXlu1TAuVchdsfXMzC0gS_YTeQzINVoDikaUtWmz1XpEPP2CD2U3tCTdQ2XmQoRnn7a5XlERb-vLX3sHgVyhWMqXh6cRGPfxK84Fgkj2qy27gLjRM4auM3MVC_lUvWrrg009TybvBhm4C-SqenfjrVVm1qXY1C89aefYmXV2uvkpdjiniNxa2W0yuc2mKmrJW6j-i5gjZLC96fIKi4an2GZy6CxSf1jWgqt2pa1BGdAV8OqTc1vfTVOXcPy4YV3tlxncdm-gSf_R1hWM1rMfog",
  "scope" : "user:create",
  "expires_in" : 3600,
  "refresh_token" : null
}
```

The `expires_in` indicates the token expiry time in `seconds`.

If you want to override the expiry time, add the `expiry` tag and specify the expiry time in `Minute`. For example, in the below configuration, the expiry is set to 1440 minute, i.e., 1 day.


```
    <security-constraint>
        <auth-handler>org.javalabs.decl.vertx.container.handler.AuthorizationHandler</auth-handler>
        <jwt-opts>
            <issuer>hearth</issuer>
            <expiry>1440</expiry>
        </jwt-opts>
        
        <!-- 
            NO_AUTH is a special tag, which indicates no authentication will be
            performed for the below set of url pattern(s).
        -->
        <auth-constraint>
            <auth-type>NO_AUTH</auth-type>
            <url-patterns>
                <!-- Adding CTX_ROOT will prepend the value from the context-root tag -->
                <url-pattern>{CTX_ROOT}/mgmt/</url-pattern>
            </url-patterns>
        </auth-constraint>
    </security-constraint>

```

### Step 2 - Create a User

**Payload:** `user.json`

```
{
    "fullName": "Socretes",
    "phone1" : "1-029837467382",
    "email" : "socretes@javalabs.org"
}


```

**Command:**

```
curl -i \
    -X POST \
    --cert src/main/resources/client_cert/hearth-client.crt \
    --key src/main/resources/client_cert/hearth-client.key \
    --cacert src/main/resources/ca/ca_javalabs.crt \
    -H "Authorization: Bearer {access_token}" \
    -H "Content-Type:application/json" \
    --data-binary @./user.json \
    https://localhost:9443/api/v1/users

```

**Response:**

```
{
  "externalId" : "234b8491-cc5e-4be1-82ac-ba8ac35ff6d8",
  "fullName" : "Socretes",
  "email" : "socretes@javalabs.org",
  "phone1" : "1-029837467382",
  "phone2" : null,
  "role" : "CUSTOMER",
  "status" : "ACTIVE",
  "createdAt" : 1786786731348,
  "updatedAt" : null
}

```

After the user is created, a user token can be generated for operations such as viewing the user profile and future bookings.

### Step 3 - Generate a User Token (Non-Admin)

**Command:**

```
curl -i \
    -X POST \
    --cert /path/to/hearth-app/src/main/resources/client_cert/hearth-client.crt \
    --key /path/to/hearth-app/src/main/resources/client_cert/hearth-client.key \
    --cacert /path/to/hearth-app/src/main/resources/ca/ca_javalabs.crt \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d 'phone1=1-029837467382' \
    https://localhost:9443/api/v1/mgmt/token

```

**Response:**

```
{
  "token_type" : "Bearer",
  "access_token" : "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIyMzRiODQ5MS1jYzVlLTRiZTEtODJhYy1iYThhYzM1ZmY2ZDgiLCJhdWQiOm51bGwsImlzcyI6ImZvbGtzIiwianRpIjoiODY2OWMxMTgtMjQ0OC00ZTJlLWI0MTktODM2MWZhNzNkYzU2IiwiaWF0IjoxNzg2NzkzODk1LCJleHAiOjE3ODY3OTc0OTV9.JrWo1XnaMyuqxhIxc-teHRyhTdKN0McflbdgXOSfCtIxpDdwnGkIAIFGf-usmC86rpgZj3sdgHfqMngDh_o0HWZAPLNiALPdQofJlHUM_drGdEl6L7J3eV6Sl-e4nh9KeTLBkcx3IxRkIflMMv6Q4k3AD0FKWbTJy-olkP5jj7e7uA4Kncb4l6KeqeVswiXwfYCEvZWozKsLl5rc2rzRFDZMjAKGl1UoCZ9VRvZwl43BwZQuncecHqptFZRDZtRfqc5j5SPxUsIZfrfvuCuZ95Tx_DrNfWLwP1rXrHIRJlb_gwbTnaudBQOkLyLOYz7O79g_Sv6EmrqP2Q3LJOQ4TA",
  "scope" : null,
  "expires_in" : 3600,
  "refresh_token" : null
}

```

Now Socretes will use this user access_token for any subsequent operation, e.g., View Profile, Make Booking, View Booking, Check Wallet, etc.

### Step 4 - View Own Profile

**Command:**

```
curl -i \
    --cert /path/to/hearth-app/src/main/resources/client_cert/hearth-client.crt \
    --key ~/Projects/hearth-app/src/main/resources/client_cert/hearth-client.key \
    --cacert /path/to/hearth-app/src/main/resources/ca/ca_javalabs.crt \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer {user_access_token}
    https://localhost:9443/api/v1/users/234b8491-cc5e-4be1-82ac-ba8ac35ff6d8

```

**Response:**

```
{
  "externalId" : "234b8491-cc5e-4be1-82ac-ba8ac35ff6d8",
  "fullName" : "Socretes",
  "email" : "socretes@javalabs.org",
  "phone1" : "1-029837467382",
  "phone2" : null,
  "role" : "CUSTOMER",
  "status" : "ACTIVE",
  "createdAt" : 1786786731348,
  "updatedAt" : null
}

```


#### Step 5 - View All Categories & Services

**Command:**

```
curl -i \
--cert src/main/resources/client_cert/hearth-client.crt \
--key src/main/resources/client_cert/hearth-client.key \
--cacert src/main/resources/ca/ca_javalabs.crt \
-H "Authorization:$jwt" \
-H "Content-Type:application/json" \
"https://localhost:9443/api/v1/categories/hierarchy?id=101&id=102&id=103&id=104&id=105"        

```

**Response:**

```
{
  "total" : 5,
  "items" : [ {
    "categoryId" : 101,
    "name" : "Salon & Makeup",
    "icon" : "scissors",
    "tagLine" : "Professional grooming and beauty services at home.",
    "image" : "https://images.unsplash.com/photo-1634449571010-02389ed0f9b0?w=800&q=80&auto=format&fit=crop",
    "parentId" : null,
    "subCategories" : [ {
      "categoryId" : 106,
      "name" : "Women's Salon",
      "icon" : null,
      "tagLine" : null,
      "image" : "https://images.unsplash.com/photo-1634449571010-02389ed0f9b0?w=800&q=80&auto=format&fit=crop",
      "parentId" : null,
      "subCategories" : null,
      "parent" : null,
      "services" : [ {
        "serviceId" : 271,
        "categoryId" : null,
        "name" : "Fruit Facial Glow",

    ....
    ....
    ....

```


## Keystore Handling

`hearth-app` uses two types of key material:
1. `hearth.pkcs` - Used for JWT signing and validation.
2. PEM keys and certificates - Used for TLS and mTLS.

### Set Up the Browser-Facing Certificate for Node.js

#### Step 1 - Create a CSR and private key for the Node server facing browser 

```
openssl req -new -newkey rsa:2048 -nodes \
  -keyout node-ext.key \
  -out node-ext.csr \
  -subj "/CN=www.hearth.com"

```

#### Step 2 - Create SAN + serverAuth extensions

```
subjectAltName = DNS:node.example.internal,DNS:localhost,IP:127.0.0.1
extendedKeyUsage = serverAuth
keyUsage = digitalSignature, keyEncipherment

```

#### Step 3 - Sign with your CA

```
openssl x509 -req \
  -in node-ext.csr \
  -CA ca_javalabs.crt \
  -CAkey ca_javalabs.key \
  -CAcreateserial \
  -out node-ext.crt \
  -days 825 \
  -sha256 \
  -extfile node-ext.ext

```

#### Step 4 - Verify Certificate

```
openssl verify -CAfile ca_javalabs.crt node-ext.crt

```

### Setup mTLS

This is how the end-to-end communication would look like.

```
Browser
    │ HTTPS
    ▼
Node.js        https://localhost:8443
    │ mTLS
    ▼
Vert.x         https://localhost:9443

```

For local development, create:

```
certs/
├── ca.key
├── ca.crt
├── hearth-app.key
├── hearth-app.csr
├── hearth-app.crt
├── hearth-ui.key
├── hearth-ui.csr
└── hearth-ui.crt
```

The flow is:

1. Create your own Certificate Authority (CA).
2. Use the CA to sign the server certificate.
3. Use the CA to sign the client certificate.
4. Configure:
   - Vert.x with `hearth-app.key`, `hearth-app.crt`, and `ca.crt`.
   - Node.js with `hearth-ui.key`, `hearth-ui.crt`, and `ca.crt`.

#### Step 1 - Create a CA

```
openssl genrsa -out ca_javalabs.key 4096
```

```
openssl req -x509 \
    -new \
    -nodes \
    -key ca_javalabs.key \
    -sha256 \
    -days 3650 \
    -out ca_javalabs.crt \
    -subj "/C=IN/ST=West Bengal/L=Kolkata/O=Javalabs/CN=Javalabs CA"
```

#### Step 2 - Create the Server Certificate

**Generate private Key:**

```
openssl genrsa -out hearth-app.key 2048
```

**Generate a certificate signing request (CSR):**

```
openssl req \
    -new \
    -key hearth-app.key \
    -out hearth-app.csr \
    -subj "/C=IN/ST=West Bengal/L=Kolkata/O=Zetachron Technologies LLP/CN=Hearth App"
```

**Create a file named server.ext:**

```
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=DNS:localhost,IP:127.0.0.1
```

Although an `.ext` file may not always be mandatory, it is highly recommended for TLS and mTLS certificates.
The `.ext` file tells OpenSSL which extensions should be embedded in the X.509 certificate.

Without an `.ext` file, OpenSSL can create a certificate that does not contain important extensions such as:

* Subject Alternative Name (SAN)
* Extended Key Usage
* Key Usage
* Basic Constraints

Modern TLS implementations rely on these extensions.

The following attributes are defined in `server.ext`:

* basicConstraints - `CA:FALSE`

This specifies that the server certificate cannot act as a Certificate Authority.
The CA certificate is the certificate that should be configured as a Certificate Authority; the server certificate remains `CA:FALSE`.

* keyUsage - `digitalSignature,keyEncipherment`

This specifies what the key may be used for.
For an HTTPS server, these usages are appropriate. Without them, some clients will reject the certificate.

* extendedKeyUsage - `serverAuth`

This extension tells clients that:

The certificate is intended to authenticate a TLS server. If a client-only certificate is used as the server certificate, 
many TLS stacks will reject it.

* subjectAltName (SAN) - `DNS:localhost,IP:127.0.0.1`

This extension is essential for hostname verification.

Older clients commonly used the Common Name (CN), such as `CN=localhost`.
Modern TLS clients verify the hostname against the Subject Alternative Name (SAN) extension.

For example: 
1. https://localhost:8443 requires: DNS:`localhost`

1. https://127.0.0.1:8443 requires: `IP:127.0.0.1`

If the SAN is missing, you'll typically see hostname verification failures.

**Sign it:**

```
openssl x509 \
    -req \
    -in hearth-app.csr \
    -CA ca_javalabs.crt \
    -CAkey ca_javalabs.key \
    -CAcreateserial \
    -out hearth-app.crt \
    -days 365 \
    -sha256 \
    -extfile server.ext
```

#### Step 3 - Create the Client Certificate

**Generate the key:**

```
openssl genrsa -out hearth-ui.key 2048
```

**Generate the CSR:**

```
openssl req \
    -new \
    -key hearth-ui.key \
    -out hearth-ui.csr \
    -subj "/C=IN/ST=West Bengal/L=Kolkata/O=Zetachron Technologies LLP/CN=Hearth UI"
```

**Create client.ext:**

```
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=clientAuth
```

**Sign it:**

```
openssl x509 \
    -req \
    -in hearth-ui.csr \
    -CA ca_javalabs.crt \
    -CAkey ca_javalabs.key \
    -CAcreateserial \
    -out hearth-ui.crt \
    -days 365 \
    -sha256 \
    -extfile client.ext
```

#### Final Set of Files

```
certs/
│
├── ca.crt
├── ca.key
│
├── server.key
├── server.crt
│
├── client.key
└── client.crt
```

The certificate-generation process also creates additional files:

1. `.csr` — Certificate Signing Request

This file contains:

1. The public key.
1. Information about the subject (Common Name, Organization, etc.).
1. A digital signature created with the corresponding private key.

It does **not** contain the private key.

For example:
`client.csr` contains a request like:

```
CN=hearth-ui
O=Zetachron Technologies LLP
C=IN
Public Key=...
```

A CSR is submitted to a Certificate Authority (CA), which signs it and issues a certificate.

The flow is:

```
client.key
      │
      ▼
Generate CSR
      │
      ▼
hearth-ui.csr
      │
      ▼
CA signs it
      │
      ▼
hearth-ui.crt
```

After `hearth-ui.crt` has been issued, the CSR is generally not required unless the certificate needs to be reissued.

2. `.srl` — Serial Number File

This file stores certificate serial-number information used by the CA.


### Generate Keystore for JWT Signing and Validation

```
keytool -genkeypair \
    -alias RS256 \
    -keyalg RSA \
    -sigalg SHA256withRSA \
    -keysize 2048 \
    -validity 365 \
    -keystore hearth.pkcs \
    -storetype PKCS12 \
    -storepass secret \
    -keypass secret \
    -dname "CN=Hearth App, OU=Development, O=Zetachron Technologies LLP, L=Kolkata, S=West Bengal, C=IN"

```

**Note:** The alias name must be one of "RS256", "RS384", "RS512", "ES256K", "ES256", "ES384", "ES512".

### View the Keystore

```
keytool  -list -v -keystore hearth.pkcs -storepass secret

```

### Additional Options

#### Extract the Public Key to .pem File

```
keytool -exportcert -rfc \
    -alias fks_dev \
    -keystore hearth.pkcs \
    -file hearth_pub.pem \
    -storepass secret 

```

#### Extract the Private Key to .pem File

```
openssl pkcs12 -in hearth.pkcs -nodes -nocerts -out hearth_prv.pem 
Enter Import Password:

```

## Test the service



The service exposes a REST API. Depending on the application configuration, data can be backed by a relational database such as PostgreSQL.

#### Create an element

```
curl -X POST\
     -d '<payload>'\
     -H "Content-Type:application/json"\
     http://localhost:8080/api/v1/<resource_name>s
```


#### Update an element

```
curl -X PUT\
     -d '<payload>'\
     -H "Content-Type:application/json"\
     http://localhost:8080/api/v1/<resource_name>/{id}
```

#### View all elements

```
curl -X GET\
     -H "Content-Type:application/json"\
     http://localhost:8080/api/v1/<resource_name>
```

#### View specific element

```
curl -X GET\
     -H "Content-Type:application/json"\
     http://localhost:8080/api/v1/<resource_name>/{id}
```

#### Delete specific element

```
curl -X DELETE\
     -H "Content-Type:application/json"\
     http://localhost:8080/api/v1/<resource_name>/{id}
```
 

## Configuration

The service uses three configuration files located under `src/main/resources`.
 
<ul>
<li><b>vertx-web.xml</b> - This file is the core configuration file that provides configuration and deployment information for Vert.x. It's the standard name used by decl-vertx-container module as a deployment descriptor in Vert.x applications. Apart from standard vert.x configuration, this file also defines the Verticles that will be deployed.</li>
<li><b>server.xml</b> - If any of your verticles is starting an http server, then you need to create the second file server.xml, which is a configuration file for the embedded http server. It dictates how the server behaves during startup and operation. It also defines various elements like the server, services, connectors, and containers, which handle requests and manage web applications.</li>
<li><b>routing-config.xml</b> - This file defines how HTTP requests are handled based on their paths and methods. Configuration typically involves setting up routes with corresponding handlers, potentially including path parameters and request body processing</li>
</ul>
