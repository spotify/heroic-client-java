# heroic-client-java

> a java client for the heroic tsdb

The client supports synchronous and asynchronous paradigms and uses okhttp to make the http requests.

Status: **experimental** api is subject to change!

## Quick start

There are three main components of a Heroic query.

1. time range.
1. filters to match time series.
1. aggregations on how to combine and group time series.


Let's build a query that will return the requests per second for each endpoint for the artist microservice and group by the region. 

What we will do is...

1. query for 1 hour worth of timeseries.
1. filter for timeseries that match the given key + tags.
1. apply two aggregations with a 60 second bucket. first one downsamples points within the bucket using the average function. the second groups the timeseries by the region tag.


```
final HeroicClient client = new HeroicClient("http://heroic");

final MetricRequest request = new MetricRequest.Builder()
    .withRange(Relative.withTime(TimeUnit.HOURS, 1L))
    .withFilter(KeyTagFilter.of(Key.of("apollo"),
        List.of(
            Tag.of("what", "endpoint-request-rate", Operator.MATCH),
            Tag.of("stat", "1m", Operator.MATCH),
            Tag.of("application", "artist", Operator.MATCH))))
    .withAggregation(
        GroupingAggregation.forEach(new Average(Sampling.withTime(TimeUnit.SECONDS, 60))))
    .withAggregation(
        GroupingAggregation.groupBy(List.of("region"), new Sum(Sampling.withTime(TimeUnit.SECONDS, 60))))
    .build();


// blocking call
final MetricResponse response = client.queryMetricsBlocking(request);

for (final ResultGroup.Points series : response.getPoints()) {
    System.out.println(series.getTags());
    for (final DataPoint point : series.getValues()) {
        System.out.println(point.getTimestamp() + ":" + point.getValue());
    }
}


// or make an async call
final CompletableFuture<MetricResponse> response = client.queryMetrics(request);


```



## Config

Setting a clientId is useful when used in conjunction with Heroic's querylogs, allowing you to understand who is responsible for the query volume.

```
final Config config = new Config.Builder()
    .setClientId("quota-watcher") 
    .setConnectTimeoutSeconds(30) // default 10s
    .setReadTimeoutSeconds(60) // default 300s
    .build();

final HeroicClient client = new HeroicClient("http://heroic", config);

```



## Code of Conduct

This project adheres to the [Open Code of Conduct][code-of-conduct]. By 
participating, you are expected to honor this code.


[code-of-conduct]: https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md
