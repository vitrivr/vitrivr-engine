# V3C Mini master-shot ingestion

This schema indexes CLIP, SigLIP 2, VLM keyframe captions, OCR, and ASR. It also stores the source file and the original TSV start and end timestamps in nanoseconds. `MasterShotBoundaryDecoder` matches each video basename to its TSV, takes the midpoint frame of each inclusive frame range, and decodes the corresponding audio to 16 kHz mono for ASR.

The pipeline reads `/home/ubuntu/v3c_mini/v3c` and `/home/ubuntu/v3c_mini/msb_v3c`. The included schema uses PostgreSQL with pgvector on port 5432, the Python descriptor service on port 8888, and `vitrivr-vlm` on port 8000. Adjust the connection and field settings in [schema.json](schema.json) if your services use different addresses or credentials.

Build and start the engine from the repository root.

```bash
bash gradlew :vitrivr-engine-server:installDist
vitrivr-engine-server/build/install/vitrivr-engine-server/bin/vitrivr-engine-server example-configs/v3c-mini/schema.json
```

In the engine shell, initialize the schema and start the [master-shot pipeline](video-ingest.json).

```text
v3cmini init
v3cmini extract -n mastershots
```

The supplied `00001.tsv` defines 114 master shots. A fresh ingest creates those shots and one source video. Re-running ingestion creates new shot IDs, so use a fresh schema for another full ingest if you want to avoid duplicate shots.

The VLM field in this pipeline uses the representative frame. You can add complete-shot captions as a separate field using the [video-caption setup](../vlm/README.md#caption-complete-video-shots).

The pipeline exports each midpoint frame as a JPEG thumbnail named `<shot UUID>.jpg` under `example-configs/v3c-mini/thumbnails/`. The maximum side is 500 pixels. Set the `thumbnails` resolver location in the schema to use a different output directory.
