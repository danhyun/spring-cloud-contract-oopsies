# Issues working with Spring Cloud Contract + Gradle Plugin + YAML Dsl



## Issue 1

Malformed YAML does not fail the `generateContractTests` task.
The task fails silently and reports `BUILD SUCCESSFUL`

Refer to `src/test/resources/contracts/rest/foo/shouldFailBuild.yml`

```yaml
request:
  method: POST
  headers:
    content-type: application/json
  url: /foo
  bodyFromFile: > # <-- invalid file name doesn't fail build
    [{
      "bar": {
        "baz": true
      }
    }]
response:
  status: 200
  headers:
    content-type: application/json;charset=UTF-8
  body:
  - foo.bar.baz: true
```

### Reproduce:

`$./gradlew clean genConTests -d`


### Notes:

Details around failure to convert are buried so you must use `-d` flag to see any evidence of failure to produce tests.

```
> Task :generateContractTests
Task ':generateContractTests' is not up-to-date because:
 Task has not declared any outputs despite executing actions.
Generated test sources dir [/home/dhyun/tmp/cloud-contracts/build/generated-test-sources/contracts]
Spring Cloud Contract Verifier Plugin: Invoking test sources generation
Contracts are unpacked to [/home/dhyun/tmp/cloud-contracts/build/stubs/META-INF/cloud-contracts/unspecified/contracts]
Included contracts are [.*]
Registering /home/dhyun/tmp/cloud-contracts/build/generated-test-sources/contracts as test source directory
Creating new class file [/home/dhyun/tmp/cloud-contracts/build/generated-test-sources/contracts/hello/verifier/rest/FooTest.java]
Generated 1 test classes
:generateContractTests (Thread[Task worker for ':',5,main]) completed. Took 0.017 secs.

BUILD SUCCESSFUL in 0s
   ```

## Issue 2

YAML dsl converter seems unable to interpret response body assertions if in a list format.

Refer to `src/test/resources/contracts/rest/foo/shouldGenerateResponseAssertions.yml`

```yaml
description: this doesn't generate json response assertions for body
request:
  method: POST
  headers:
    content-type: application/json
  url: /foo
  body: >
    [{
      "bar": {
        "baz": true
      }
    }]
response:
  status: 200
  headers:
    content-type: application/json;charset=UTF-8
  body:
  - foo: # <--- YAML dsl converter needs help here
      bar:
        baz: true
```

### Notes:

No failure message propagated to build without `-d` and no generated test method.

## Issue 3

YAML dsl for response body matchers does not generate corresponding assertions for response body.

Refer to ```src/test/resources/contracts/rest/foo/shouldGenerateResponseAssertionsForMatchers.yml```

```yaml
description: this doesn't generate json response assertions for body matchers
request:
  method: POST
  headers:
    content-type: application/json
  url: /foo
  body: >
    [{
      "bar": {
        "baz": true
      }
    }]
response:
  status: 200
  headers:
    content-type: application/json;charset=UTF-8
  matchers:
    body:
    - path: $..foo.bar.baz  # <--- where did this assertion go?
      type: by_regex
      value: (tru|fals)e
```

### Notes:

Test method generated but no corresponding assertions where added, nothing in output seems immediately helpful in resolving.

### Generated Test

```java
public class FooTest extends MvcTest {

	@Test
	public void validate_shouldGenerateResponseAssertionsForMatchers() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("content-type", "application/json")
					.body("[{\"bar\":{\"baz\":true}}]");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/foo");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("content-type")).isEqualTo("application/json;charset=UTF-8");
			
        // Where did my response body matchers assertions go?
	}

}

```
