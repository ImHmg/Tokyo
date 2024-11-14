# Tokyo

**Tokyo** is a codeless API testing tool that simplifies API testing for developers. Define YAML spec files for requests, create scenario files by combining requests, and run them with CSV inputs. Tokyo supports environment configurations, fake data generation, assertions, response capturing, and detailed reporting—no coding required, just configure and test!

## Installation

### Prerequisites
- **Java Development Kit (JDK)**: Make sure you have JDK installed (version 11 or higher).
- **Gradle**: Ensure Gradle is installed or included with your IDE.

### Step 1: Adding Tokyo to Your Project
Add Tokyo as a dependency in your project's `build.gradle` file:

```groovy
dependencies {
    implementation 'io.github.imhmg:tokyo:1.0.0' 
}
```

### Step 2: Create Sample Request File

Create a sample request file in the `src/test/resources` directory. This file will define your API request.

1. **Create a file named `add-object.yaml`** in the `src/test/resources` directory with the following content:

```yaml
name: Add Object
method: POST
endpoint: https://example.com/api/object
status: 200
jsonBody: |
  {
    "name": "Object name"
  }
```

### Step 3: Create Scenario Flow YAML

Next, create a scenario flow file that defines the sequence of requests. This file will reference the previously created request file.

1. **Create a file named `object-scenario.yaml`** in the `src/test/resources` directory with the following content:

```yaml
name: Object Scenario
description: Scenario for adding an object

steps:
  - id: add-object
    name: Add Object
    ref: add-object.yaml
```

### Step 4: Configure Scenario Flow in Java

Create a Java class to configure and run the scenario flow. The class will extend `TokyoRunner` and specify the necessary configuration.

1. **Create a Java class** named `ObjectTest.java` with the following content:

```java
import io.github.imhmg.tokyo.TokyoRunner;
import io.github.imhmg.tokyo.RunSpec;

import java.util.List;
import java.util.Map;

public class ObjectTest extends TokyoRunner {
    static {
        TokyoRunner.addRunSpec(RunSpec.builder()
                        .scenarioSpecFile("object-scenario.yaml")  // Path to your scenario file
                        .build());
    }
}
```

### Step 5: Run Tests via Gradle

You can run the tests either via Gradle or directly in IntelliJ.

   ```bash
   ./gradlew test --tests ObjectTest
```

## Table of Contents

1. [Introduction](#introduction)
2. [Request Spec File Format](#request-spec-file-format)
   1. [Name](#name)
   2. [Method](#method)
   3. [Endpoint](#endpoint)
   4. [Status](#status)
   5. [Headers](#headers)
   6. [TextBody](#textbody)
   7. [JsonBody](#jsonbody)
   8. [FormParams](#formparams)
   9. [QueryParams](#queryparams)
   10. [Captures](#captures)
   11. [Asserts](#asserts)
   12. [Define](#define)
   13. [MultipartData](#multipartdata)
   14. [Options](#options)
3. [Example Request Spec File](#example-request-spec-file)
4. [Using the Request Spec in a Scenario](#using-the-request-spec-in-a-scenario)
5. [Configuration and Customization](#configuration-and-customization)
6. [Best Practices](#best-practices)



## Introduction

The **request spec file** defines a template for API requests, specifying the HTTP method, endpoint, body, headers, parameters, and assertions. It enables developers to easily configure and reuse API requests in automated tests, ensuring consistent and efficient test execution across different scenarios.

### Name

The **name** field specifies a unique identifier for the request. It helps differentiate between multiple requests within a test suite.
```yaml
Name: Hello World
```

### Method

The **method** field specifies the HTTP method to be used for the request.
Example:

```yaml
Name: Hello World
Method: POST
```

### Endpoint

The **endpoint** field specifies the URL of the API resource to which the request is being made. It should include the full URL path.

Example:

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/posts
```

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/posts/${postId}
```
```yaml
Name: Hello World
Method: POST
Endpoint: ${serverUrl}/posts/${postId}
```

### Status

The **Status** field specifies the expected HTTP status code for the response. It will be asserted against the actual response status code to verify that the API behaves as expected.

Example:

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/api/resource
Status: 200
```

### Headers

The **Headers** field specifies any HTTP headers to be included with the request. 

Example:

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/api/resource
Headers:
  Content-Type: application/json
  Authorization: Bearer ${authorizationToken}
```

### TextBody

The **TextBody** field specifies the body content of the request in plain text format. It can be used when the request body is not in JSON or XML format and requires raw text to be sent to the server.

Example:

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/api/resource
Headers:
  Content-Type: text/plain
TextBody: |
  This is a sample plain text request body.
```

### JsonBody

The **JsonBody** field specifies the body content of the request in JSON format. It is similar to the **TextBody**, but when using **JsonBody**, the `Content-Type: application/json` header is automatically added.

Example:

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/api/resource
JsonBody: |
  {
    "key": "value",
    "anotherKey": "anotherValue"
  }
```
```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/api/resource
JsonBody: |
  {
    "key": "${inputKey}",
    "anotherKey": "${inputValue}"
  }
```
### FormParams

The **FormParams** field specifies any form data parameters to be included in the request. These parameters are typically sent in `application/x-www-form-urlencoded` format, which is common for form submissions.

Example:

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/api/resource
Status: 200
FormParams:
  username: testuser
  password: ${password}
```

### QueryParams

The **QueryParams** field specifies the query parameters to be included in the request URL. 

Example:

```yaml
Name: Hello World
Method: GET
Endpoint: https://example.com/api/resource
Status: 200
QueryParams:
  search: test
  limit: ${limit}
```

### Asserts

The **Asserts** field specifies the conditions to verify in the API response. Assertions are used to validate that the API response meets the expected criteria, such as checking for specific values, status codes, or other conditions.

Example:

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/api/resource
Status: 200
JsonBody: |
  {
    "key": "123",
    "value": "${value}"
  }
Asserts:
   "key equals to 123" : "@body json $.key [==] 123"
   "value equals to ${value}" : "@body json $.key [==] ${value}"
```

### Assert Syntax

The assert syntax follows this format:

```@<source> <capture expression> <operator> <expected value>```

- The **source** and **capture expression** are mandatory.
- If the **operator** and **expected value** are not provided, the captured value will be asserted to be **not null**.

To ensure the `authorization` header is not null:

```yaml
"authorization header not null" : "@header authorization"
```

#### Available Operators

The following operators are supported:
- `[==]`: Equality
- `[!=]`: Inequality
- `[<>]`: Contains
- `[<!>]`: Does not contain

##### Header
```@header <header-name> <operator> <expected value> ```

Example:

```yaml
"authorization header not null" : "@header authorization"
"x-user-id is 124" : "@header x-user-id [==] 123"
"Date contains 1993" : "@header Date [<>] 1993"
```

##### Body
```@body <type> <expression> <operator> <expected value> ```

The following types are supported for body:
- json
- raw
- xml [To be implemented]

When the type is `json`, the expression should use JSONPath syntax.

Example:

```yaml
"user is not null" : "@body json $.id"
"user name is Rocky" : "@body json $.data.username [==] Rocky"
"address contains LK" : "@body json $.data.address [<>] LK"
```

When the type is `raw`, No expression needed.

Example:

```yaml
"response body is OK" : "@body raw [==] OK"
"response body contains Success" : "@body raw [<>] Success"
```
##### Status

```@status <operator> <expected value> ```

Example:

```yaml
"Status is 200" : "@status [==] 200"
"Status is not 202" : "@status [!=] 202"
```

### Captures

The **Captures** field specifies the response values to be captured and stored for further use.

Example:

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/api/resource
Status: 200
JsonBody: |
  {
    "key": "123",
    "value": "${value}"
  }
Captures:
   "key" : "@body json $.key"
   "value" : "@body json $.key"
   "authorization" : "@header authorization"
```
#### Capture Syntax
```@<source> <capture expression>```

Capture syntax is similar to assert syntax but does not include an operator or expected value:

- **Source** specifies where to capture the data, such as `@body`,  `@header` or `@status`.
- **Capture expression** defines the path or key to retrieve.

Examples:

```yaml
"key" : "@body json $.key"
"value" : "@body json $.key"
"responseBody" : "@body raw"
"authorization" : "@header authorization"
"statusCode" : "@status"
```
### Define

The **Define** section assigns a variable value that can be used within the current request spec or in subsequent requests throughout the scenario flow.

```yaml
Name: Hello World
Define:
   server: "example.com"
   key: "${faker regexify '[a-z]{10}'}" # Generate random 10 character string with a - z
   value: "${faker regexify '[0-9]{3}'}" # Generate random 3 digit numbers
Method: POST
Endpoint: https://${server}/api/resource
Status: 200
JsonBody: |
  {
    "key": "${key"",
    "value": "${value}"
  }
```

### Multipart

The **Multipart** section is intended for handling multipart form data, allowing multiple parts in a single request, such as files or form fields. This feature is currently not yet implemented.

### Options

The **Options** section provides additional configuration settings for customizing request behavior.

#### Example Options

```yaml
Name: Hello World
Method: POST
Endpoint: https://example.com/api/resource
Status: 200
JsonBody: |
  {
    "key": "123",
    "value": "${value}"
  }
Options:
  timeout : "5000"          # Timeout in milliseconds
  insecured : true          # Disable ssl verification