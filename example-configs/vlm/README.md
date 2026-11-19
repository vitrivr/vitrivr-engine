# VLM caption retrieval

`VlmCaption` generates one description per master-shot keyframe during ingestion. PostgreSQL stores the description and ranks text matches against a dedicated caption index. The query result includes the original shot ID, lexical relevance score, caption, and timestamps from the existing `time` field.

## Enable on an existing video schema

1. Merge the `vlmcaption` field from [schema.json](schema.json) into the PostgreSQL schema you use for video indexing. Keep its `FULLTEXT` index on `caption`, and set `baseUrl` to the OpenAI-compatible API base, including `/v1` if the server uses it. Set `model` to the served model identifier. For the Qwen setup, use `http://localhost:8000/v1` when the engine can reach vLLM on its own loopback interface, and set `model` to `vitrivr-vlm`.
2. Reload the schema and run `<schema> init` in the engine shell. This creates the caption table and index alongside the existing features.
3. Feed the field one image per shot, after the shot entity is persisted. The upstream master-shot pipeline supplies the representative frame and assigns the shot ID. Keep the existing `TemporalMetadata` operation that writes the shot's start and end in nanoseconds. An input with no image passes through, and an input with multiple images is skipped with a log message.

For an existing ingestion DAG, add the operator and insert its operation after the stage that supplies the persisted shot and its representative keyframe. In this fragment, `indexedShot` is the name of that existing stage. Connect `caption` to the next operation or make it an output.

```json
{
  "operators": {
    "vlmcaption": {"field": "vlmcaption"}
  },
  "operations": {
    "caption": {"operator": "vlmcaption", "inputs": ["indexedShot"]}
  }
}
```

## Index or refresh captions from stored keyframes

Use [index-captions.json](index-captions.json) if the shot entities already exist and their representative frames are available as image files. Set the schema name in the pipeline and register it under `extractionPipelines`, as shown in [schema.json](schema.json). Point the `masterKeyframes` disk resolver at files named `<shot UUID>.png`. Set `suffix` to `jpg` or `jpeg` for JPEG files and set `shotType` to the type used for the stored master shots.

Run `<schema> extract -n vlm-captions` in the engine shell. The pipeline reads the images by persisted shot ID and writes captions in place. Missing images are skipped. Each field keeps one caption per shot ID. A model or prompt refresh replaces that caption and its provenance after successful generation. Failed VLM calls log the affected shot ID, preserve any previous caption, and let ingestion continue. Re-run the caption pipeline after restoring the service. Reusing the original shot IDs matters because some decoder and segmentation pipelines assign new IDs when replayed.

## Search

Submit [query-captions.json](query-captions.json) to the engine with your text in `inputs.text.data`.

```bash
curl --fail --json @example-configs/vlm/query-captions.json "$ENGINE_URL/api/$SCHEMA/query"
```

PostgreSQL normalizes the query with the configured text-search dictionary, requires the resulting terms to match, and ranks matches with `ts_rank` before applying the result limit. `FieldLookup` attaches `time.start` and `time.end` to those same shot IDs. The result includes `vlmcaption.caption`, `vlmcaption.model`, `vlmcaption.prompt_version`, and `vlmcaption.generation_metadata`. The last field is a JSON string with the generation time, request settings, and any response ID, finish reason, and usage provided by the server. Queries can run while the VLM is offline.

## Settings

Field parameters are strings, as in the other schema fields. Indexing parameters can also be overridden in the ingestion operator keyed by the field name.

| Setting | Meaning |
| --- | --- |
| `baseUrl` | Required for enabled indexing. The client appends `/chat/completions`. |
| `model` | Required for enabled indexing. The identifier sent to the API. |
| `prompt` | Defaults to the concise factual description prompt. Set this to replace the complete instruction. |
| `promptVersion` | Defaults to `1`. Change it when the prompt changes. |
| `maxOutputTokens` | Defaults to `128`. The request uses temperature zero. |
| `requestTimeoutMs` | Defaults to `60000` per request. |
| `maxConcurrentRequests` | Defaults to `1` per indexing operator. Use a value your deployment can serve concurrently. |
| `disableThinking` | Defaults to `false`. Set it to `true` for Qwen deployments that support the vLLM chat template option `enable_thinking=false`. |
| `enabled` | Defaults to `true`. Set it to `false` to pause generation while continuing to search existing captions. |
| `limit` | Defaults to `1000`. The retrieval operation can override it. |
| `language` | A `FULLTEXT` index parameter. Defaults to PostgreSQL's `english` dictionary. Use `simple` for token matching without stemming. |
| `gin` | A `FULLTEXT` index parameter. Set it to `true` when initializing the field to create a GIN index. |

## Test

Run the mocked HTTP client tests with Gradle. The pipeline test uses the shipped example configs, a mocked VLM, and a PostgreSQL instance with the pgvector extension. It follows the repository's test credentials, `postgres` with password `vitrivr`, and defaults to port 5432. It creates a separate test schema.

```bash
./gradlew :vitrivr-engine-module-features:test
./gradlew :vitrivr-engine-module-pgvector:test --tests '*VlmCaptionIntegrationTest.captionsAreUpsertedAndRankedWithOriginalShotMetadata'
```

To manually test the live Qwen service, run the opt-in test below with PostgreSQL available. Set `VITRIVR_TEST_PG_PORT` if it uses a different port. The test indexes a red image with Qwen and searches the persisted description for “red”. It checks the original shot ID, both timestamps, and a positive score.

```bash
VITRIVR_LIVE_VLM_URL=http://localhost:8000/v1 VITRIVR_LIVE_VLM_MODEL=vitrivr-vlm \
  ./gradlew :vitrivr-engine-module-pgvector:test --tests '*VlmCaptionIntegrationTest.indexesAndRetrievesWithLiveVlm'
```

## Caption complete video shots

Add the separate `vlmvideocaption` field from [video-schema.json](video-schema.json). It sends the full MP4 for each shot to the configured server as an inline `video_url`. The prompt asks for visible actions and changes in order. The model and server decide how to sample the video internally. This feature uses the visual track for the caption.

For shots already indexed, place each clip at `<shot UUID>.mp4` under the `masterClips` resolver. Register [index-video-captions.json](index-video-captions.json) as shown in the schema example. Reload the schema, run `<schema> init`, then run `<schema> extract -n vlm-video-captions`. Set `shotType` in both the field and ingestion source if your shots use a different type.

For inline indexing, the existing `VideoSegmentExporter` can produce these files. Configure it with the same `masterClips` resolver, `mimeType: MP4`, `video: true`, `audio: false`, and `keyFrames: false`. Include the resolver in the ingestion context. Run the exporter on persisted shots carrying the original video source and time range, then connect its output to the `vlmvideocaption` field operation.

The field shares the endpoint, model, prompt version, and other settings above. `videoResolver` names the clip resolver. `maxVideoBytes` defaults to 16 MiB before Base64 encoding. Missing, empty, oversized, or rejected clips are logged and skipped, and any previously stored caption is kept. [query-video-captions.json](query-video-captions.json) searches the separate video-caption index using the same shot IDs and timestamps.

The live smoke test uses the repository's red–green–blue MP4 and checks that searching for “green” returns the original shot and timestamps.

```bash
VITRIVR_LIVE_VLM_URL=http://localhost:8000/v1 VITRIVR_LIVE_VLM_MODEL=vitrivr-vlm \
  ./gradlew :vitrivr-engine-module-pgvector:test --tests '*VlmCaptionIntegrationTest.indexesAndRetrievesVideoWithLiveVlm'
```
