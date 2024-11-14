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
    implementation 'com.example:tokyo:1.0.0' // Replace with the actual version
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
