# Text Preprocessing Microservice

ARDoCo performs natural language processing (NLP) as part of its text preprocessing pipeline. While preprocessing can run locally, ARDoCo also supports using a **microservice** for text preprocessing, which offers several advantages.

## Benefits of the Microservice

- **Faster Performance**: NLP models remain loaded in memory, eliminating model loading overhead for each execution
- **Reduced Local Memory**: Models are hosted remotely rather than consuming local RAM
- **Shared Infrastructure**: Multiple ARDoCo instances can share a single preprocessing service
- **Consistent Results**: Centralized preprocessing ensures uniform text analysis across executions

## Microservice Repository

The text preprocessing microservice is available at:
- **GitHub**: [ardoco/StanfordCoreNLP-Provider-Service](https://github.com/ardoco/StanfordCoreNLP-Provider-Service/)

This service wraps the Stanford CoreNLP library and exposes it as a REST API for text analysis tasks including:
- Tokenization
- Part-of-speech tagging
- Dependency parsing
- Named entity recognition
- Lemmatization

## Configuration

The microservice uses credential-based authentication and requires configuration via environment variables.

### Environment Variables

To use the microservice, set the following environment variables:

```bash
NLP_PROVIDER_SOURCE=microservice
MICROSERVICE_URL=[your_microservice_url]
SCNLP_SERVICE_USER=[your_username]
SCNLP_SERVICE_PASSWORD=[your_password]
```

**Variable Details**:

- **`NLP_PROVIDER_SOURCE`**: Activates microservice usage
  - Set to `microservice` to enable remote preprocessing
  - Default (if not set): local preprocessing

- **`MICROSERVICE_URL`**: URL of your deployed microservice instance
  - Example: `https://nlp-service.example.com:8080`

- **`SCNLP_SERVICE_USER`**: Authentication username for the microservice
  - Must match the credentials configured in your deployment

- **`SCNLP_SERVICE_PASSWORD`**: Authentication password for the microservice
  - Must match the credentials configured in your deployment

### Example Configuration

Create a `.env` file or export environment variables:

```env
NLP_PROVIDER_SOURCE=microservice
MICROSERVICE_URL=https://nlp.myorg.com:8080
SCNLP_SERVICE_USER=ardoco_user
SCNLP_SERVICE_PASSWORD=secure_password_123
```

Then run ARDoCo with these variables set, and it will automatically use the configured microservice for text preprocessing.

## Local vs. Microservice Comparison

| Aspect | Local Preprocessing | Microservice Preprocessing |
|--------|-------------------|---------------------------|
| **Setup** | No additional setup required | Requires service deployment |
| **Memory Usage** | High (models loaded locally) | Low (models hosted remotely) |
| **Startup Time** | Slower (model loading) | Faster (models pre-loaded) |
| **Network Dependency** | None | Requires network connectivity |
| **Best For** | Standalone, occasional use | Production, frequent executions |

## Deployment

For information on deploying the microservice, refer to the [StanfordCoreNLP-Provider-Service documentation](https://github.com/ardoco/StanfordCoreNLP-Provider-Service/blob/main/README.md).

## Related Documentation

- [Pipeline Architecture](pipeline) - Learn how text preprocessing fits into ARDoCo's pipeline
- [Intermediate Artifacts](intermediate-artifacts) - Understand the text representation format
- [Traceability Link Recovery](traceability-link-recovery) - See how preprocessing enables TLR approaches
