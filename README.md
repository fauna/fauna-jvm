# fauna-jvm
FQL v10 Java Driver

## Using the Driver
### Installation
Download from the Maven central repository:

File `fauna-java/pom.xml`:
```xml
<dependencies>
  ...
  <dependency>
    <groupId>com.fauna</groupId>
    <artifactId>fauna-java</artifactId>
    <version>1.0</version>
  </dependency>
  ...
</dependencies>
```

#### Basic Java Usage
```java
import com.fauna.Client;
import com.fauna.FQL;
import com.fauna.QuerySuccess;

/**
 * Connect to Fauna and creates a new database.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // The client defaults to using environment variable FAUNA_SECRET.
        Client client = Client();

        // You can specify the secret using
        // Client.builder().withSecret(...)

        QuerySuccess create_dogs = client.query(FQL("Collection.create({ name: 'Dogs' })"));
        QuerySuccess create_scout = client.query(FQL("Dogs.create({ name: 'Scout' }"));
    }
}
```