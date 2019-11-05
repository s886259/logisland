package com.hurence.webapiservice.http.grafana;


import com.hurence.logisland.timeseries.sampling.SamplingAlgorithm;
import com.hurence.webapiservice.historian.reactivex.HistorianService;
import com.hurence.webapiservice.historian.util.HistorianResponseHelper;
import com.hurence.webapiservice.timeseries.GrafanaTimeSeriesModeler;
import com.hurence.webapiservice.timeseries.TimeSeriesRequest;
import com.hurence.webapiservice.timeseries.TimeSeriesModeler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hurence.logisland.timeseries.sampling.SamplingAlgorithm.AVERAGE;
import static com.hurence.logisland.timeseries.sampling.SamplingAlgorithm.FIRST_ITEM;
import static com.hurence.webapiservice.http.Codes.BAD_REQUEST;
import static com.hurence.webapiservice.http.Codes.NOT_FOUND;

public class GrafanaApiImpl implements GrafanaApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrafanaApiImpl.class);
    private HistorianService service;
    private static final QueryRequestParser queryRequestParser = new QueryRequestParser();
    private TimeSeriesModeler timeserieToolBox = new GrafanaTimeSeriesModeler();

    public final static String ALGO_TAG_KEY = "Algo";
    public final static String BUCKET_SIZE_TAG_KEY = "Bucket size";


    public GrafanaApiImpl(HistorianService service) {
        this.service = service;
    }

    @Override
    public void root(RoutingContext context) {
        context.response()
                .setStatusCode(200)
                .end("Historian grafana api is Working fine");
    }

    @Override
    public void search(RoutingContext context) {
        //TODO parse request body to filter query of metrics ?
        final JsonObject getMetricsParam = new JsonObject();
        service.rxGetMetricsName(getMetricsParam)
                .doOnError(ex -> {
                    LOGGER.error("Unexpected error : ", ex);
                    context.response().setStatusCode(500);
                    context.response().putHeader("Content-Type", "application/json");
                    context.response().end(ex.getMessage());
                })
                .doOnSuccess(metricResponse -> {
                    JsonArray metricNames = metricResponse.getJsonArray(HistorianService.METRICS);
                    context.response().setStatusCode(200);
                    context.response().putHeader("Content-Type", "application/json");
                    context.response().end(metricNames.encode());
                }).subscribe();
    }

    @Override
    public void query(RoutingContext context) {
        final TimeSeriesRequest request;
        try {
            JsonObject requestBody = context.getBodyAsJson();
            request = queryRequestParser.parseRequest(requestBody);
        } catch (Exception ex) {
            LOGGER.error("error parsing request", ex);
            context.response().setStatusCode(BAD_REQUEST);
            context.response().setStatusMessage(ex.getMessage());
            context.response().putHeader("Content-Type", "application/json");
            context.response().end();
            return;
        }

        final JsonObject getTimeSeriesChunkParams = buildHistorianRequest(request);

        service
                .rxGetTimeSeriesChunk(getTimeSeriesChunkParams)
                .map(chunkResponse -> {
                    List<JsonObject> chunks = HistorianResponseHelper.extractChunks(chunkResponse);
                    Map<String, List<JsonObject>> chunksByName = chunks.stream().collect(
                            Collectors.groupingBy(chunk ->  chunk.getString(HistorianService.METRIC_NAME))
                    );
                    //TODO external this in a service so that is does not block thread
                    //TODO or use blockExecuting method of vertx. What is the best choice ?
                    return TimeSeriesModeler.buildTimeSeries(request, chunksByName, timeserieToolBox);
                })
                .doOnError(ex -> {
                    LOGGER.error("Unexpected error : ", ex);
                    context.response().setStatusCode(500);
                    context.response().putHeader("Content-Type", "application/json");
                    context.response().end(ex.getMessage());
                })
                .doOnSuccess(timeseries -> {
                    context.response().setStatusCode(200);
                    context.response().putHeader("Content-Type", "application/json");
                    context.response().end(timeseries.encode());
                }).subscribe();
    }



    private JsonObject buildHistorianRequest(TimeSeriesRequest request) {
        JsonArray fieldsToFetch = new JsonArray()
                .add(HistorianService.CHUNK_VALUE)
                .add(HistorianService.CHUNK_START)
                .add(HistorianService.CHUNK_END)
                .add(HistorianService.CHUNK_SIZE)
                .add(HistorianService.METRIC_NAME);
        List<String> metricsToRetrieve = request.getNames();
        return new JsonObject()
                .put(HistorianService.FROM, request.getFrom())
                .put(HistorianService.TO, request.getTo())
                .put(HistorianService.MAX_TOTAL_CHUNKS_TO_RETRIEVE, 1000)
                .put(HistorianService.FIELDS_TO_FETCH, fieldsToFetch)
                .put(HistorianService.NAMES, metricsToRetrieve);
    }

    @Override
    public void annotations(RoutingContext context) {
        throw new UnsupportedOperationException("Not implemented yet");//TODO
    }

    /**
     * return every custom key parameters that can be used to query data.
     * @param context
     */
    @Override
    public void tagKeys(RoutingContext context) {
        context.response().setStatusCode(200);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(new JsonArray()
                .add(new JsonObject().put("type", "string").put("text", ALGO_TAG_KEY))
                .add(new JsonObject().put("type", "int").put("text", BUCKET_SIZE_TAG_KEY))
                .encode()
        );
    }
    /**
     * return every custom value parameters given a key that can be used to query data.
     * @param context
     */
    @Override
    public void tagValues(RoutingContext context) {
        final String keyValue;
        try {
            keyValue = parseTagValuesRequest(context);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("error parsing request", ex);
            context.response().setStatusCode(BAD_REQUEST);
            context.response().setStatusMessage(ex.getMessage());
            context.response().putHeader("Content-Type", "application/json");
            context.response().end();
            return;
        }
        final JsonArray response;
        switch (keyValue) {
            case ALGO_TAG_KEY:
                response = getTagValuesOfAlgo();
                break;
            case BUCKET_SIZE_TAG_KEY:
                //TODO verify how to handle integer type
                response = new JsonArray()
                        .add(new JsonObject().put("int", 50))
                        .add(new JsonObject().put("int", 100))
                        .add(new JsonObject().put("int", 250))
                        .add(new JsonObject().put("int", 500));
                break;
            default:
                LOGGER.warn("there is no tag with this key !");
                context.response().setStatusCode(NOT_FOUND);
                context.response().setStatusMessage("there is no tag with this key !");
                context.response().putHeader("Content-Type", "application/json");
                context.response().end();
                return;
        }
        context.response().setStatusCode(200);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(response.encode());
    }

    public String  parseTagValuesRequest(RoutingContext context) throws IllegalArgumentException {
        JsonObject body = context.getBodyAsJson();
        try {
            return body.getString("key");
        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format("body request does not contain a key 'key'. " +
                    "Request is expected to be the following format : %s \n\n but was %s",
                    "{ \"key\":\"Algo\"}", body.encodePrettily()));
        }
    }

    private JsonArray getTagValuesOfAlgo() {
        return new JsonArray()
                .add(new JsonObject().put("text", SamplingAlgorithm.NONE))
                .add(new JsonObject().put("text", SamplingAlgorithm.AVERAGE))
                .add(new JsonObject().put("text", SamplingAlgorithm.FIRST_ITEM))
                .add(new JsonObject().put("text", SamplingAlgorithm.MIN_MAX));
    }
}